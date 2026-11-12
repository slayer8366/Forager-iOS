#!/usr/bin/env python3
"""Inventory of what a MapLibre style file uses. Standard library only. Run: python3 -I style_inventory.py FILE...
Prints one JSON object per file (sorted keys) and, with --union, the union across files. No file content is echoed
beyond property names, operator names, source types, font names and URL schemes/hosts-free shapes."""
import json, sys, collections

# Properties whose array values are enumerations or literal lists, not expressions, when the first item is a string.
PLAIN_ARRAYS = {"text-font", "text-variable-anchor", "text-writing-mode", "text-variable-anchor-offset"}
# Legacy filter operators (pre-expression filters) share names with expressions; counted the same, flagged separately.
LEGACY_FILTER_ONLY = {"!in", "!has", "none"}

def walk_expr(v, ops, depth=0):
    """Count the operator of every array whose first element is a string; recurse into all arguments."""
    if isinstance(v, list):
        if v and isinstance(v[0], str):
            ops[v[0]] += 1
            if v[0] == "literal":
                return
            args = v[1:]
            if v[0] == "match":
                # ["match", input, label, output, ..., fallback]: labels (strings, numbers or arrays of them) are data.
                args = [v[1]] + [a for i, a in enumerate(v[2:]) if i % 2 == 1 or i == len(v) - 3]
            elif v[0] == "interpolate" and len(v) > 1 and isinstance(v[1], list) and v[1]:
                ops["interpolation:" + str(v[1][0])] += 1
                args = v[2:]
        else:
            args = v
        for a in args:
            walk_expr(a, ops, depth + 1)
    elif isinstance(v, dict):
        for a in v.values():
            walk_expr(a, ops, depth + 1)

def legacy_filter(f):
    """True when a filter is in the legacy form: ["==", "key", value] with a bare string key, or in/!in/has/!has."""
    if not isinstance(f, list) or not f:
        return False
    op = f[0]
    if op in ("all", "any", "none"):
        return any(legacy_filter(x) for x in f[1:])
    if op in ("in", "!in", "has", "!has"):
        return len(f) > 1 and isinstance(f[1], str) and not (op == "in" and len(f) == 3 and isinstance(f[2], list))
    if op in ("==", "!=", "<", "<=", ">", ">="):
        return len(f) == 3 and isinstance(f[1], str) and not isinstance(f[2], list)
    return False

def inventory(path):
    s = json.load(open(path, encoding="utf-8"))
    out = collections.OrderedDict()
    out["file"] = path.rsplit("/", 1)[-1]
    out["top_level_keys"] = sorted(s.keys())
    out["sources_by_type"] = dict(collections.Counter(v.get("type") for v in s.get("sources", {}).values()))
    src_keys = collections.Counter()
    src_urls = collections.Counter()
    for v in s.get("sources", {}).values():
        for k in v: src_keys[v.get("type", "?") + "." + k] += 1
        for u in ([v["url"]] if "url" in v else []) + list(v.get("tiles", [])):
            src_urls[u.split("://")[0] + ":" + u.rsplit(".", 1)[-1][:12]] += 1
    out["source_keys"] = dict(sorted(src_keys.items()))
    out["source_url_scheme_and_suffix"] = dict(src_urls)
    out["glyphs"] = bool(s.get("glyphs")); out["sprite"] = type(s.get("sprite")).__name__ if "sprite" in s else None
    layers = s.get("layers", [])
    out["layer_count"] = len(layers)
    out["layer_types"] = dict(collections.Counter(l["type"] for l in layers))
    out["layer_keys"] = dict(collections.Counter(k for l in layers for k in l))
    out["source_layers"] = len({l.get("source-layer") for l in layers if l.get("source-layer")})
    props = collections.Counter(); ops = collections.Counter(); fops = collections.Counter()
    expr_props = collections.Counter(); stop_props = collections.Counter(); stop_kinds = collections.Counter()
    fonts = collections.Counter(); enums = collections.defaultdict(collections.Counter)
    legacy = 0; tokens = 0; along = 0; text_layers = 0; icon_layers = 0; zoom_ranges = 0
    for l in layers:
        if "minzoom" in l or "maxzoom" in l: zoom_ranges += 1
        if "filter" in l:
            walk_expr(l["filter"], fops)
            if legacy_filter(l["filter"]): legacy += 1
        for grp in ("paint", "layout"):
            for k, v in l.get(grp, {}).items():
                key = l["type"] + ":" + grp + "." + k
                props[key] += 1
                if k == "text-font":
                    if isinstance(v, list) and all(isinstance(x, str) for x in v):
                        for f in v: fonts[f] += 1
                    else:
                        def lit(x):
                            if isinstance(x, list):
                                if x and x[0] == "literal" and len(x) == 2 and isinstance(x[1], list):
                                    for f in x[1]: fonts[f] += 1
                                else:
                                    for y in x: lit(y)
                            elif isinstance(x, dict):
                                for y in x.values(): lit(y)
                        lit(v)
                if isinstance(v, str):
                    if k not in ("text-field", "icon-image") or "{" not in v:
                        if k not in ("text-field", "icon-image") and not v.startswith(("#", "rgb", "hsl")): enums[k][v] += 1
                    if "{" in v: tokens += 1
                if isinstance(v, dict) and "stops" in v:
                    stop_props[key] += 1
                    stop_kinds[("property " if "property" in v else "zoom ") + v.get("type", "default") + (" base" if "base" in v else "")] += 1
                elif isinstance(v, list) and v and isinstance(v[0], str) and (
                        k not in PLAIN_ARRAYS or any(isinstance(x, list) for x in v)):
                    expr_props[key] += 1
                    walk_expr(v, ops)
                elif isinstance(v, list) and k in PLAIN_ARRAYS and k != "text-font":
                    for x in v:
                        if isinstance(x, str): enums[k][x] += 1
        lay = l.get("layout", {})
        if l["type"] == "symbol":
            if "text-field" in lay: text_layers += 1
            if "icon-image" in lay: icon_layers += 1
            if lay.get("symbol-placement") in ("line", "line-center"): along += 1
    out["properties_used"] = dict(sorted(props.items()))
    out["distinct_properties"] = len(props)
    out["properties_given_as_expression"] = sum(expr_props.values())
    out["properties_given_as_stops_function"] = sum(stop_props.values())
    out["stops_function_kinds"] = dict(stop_kinds)
    out["expression_operators_in_properties"] = dict(sorted(ops.items()))
    out["operators_in_filters"] = dict(sorted(fops.items()))
    out["layers_with_filter"] = sum(1 for l in layers if "filter" in l)
    out["layers_with_legacy_filter"] = legacy
    out["layers_with_zoom_range"] = zoom_ranges
    out["symbol_layers_with_text"] = text_layers; out["symbol_layers_with_icon"] = icon_layers
    out["symbol_layers_placed_along_line"] = along
    out["token_strings"] = tokens
    out["fonts"] = dict(fonts)
    out["enum_values"] = {k: dict(v) for k, v in sorted(enums.items())}
    return out

if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    invs = [inventory(a) for a in args]
    if "--union" in sys.argv:
        u = {"files": [i["file"] for i in invs]}
        for key in ("layer_types", "properties_used", "expression_operators_in_properties", "operators_in_filters",
                    "sources_by_type", "fonts", "stops_function_kinds", "source_keys"):
            c = collections.Counter()
            for i in invs: c.update(i[key])
            u[key] = dict(sorted(c.items()))
        u["distinct_properties"] = len(u["properties_used"])
        u["distinct_operators_all"] = sorted(set(u["expression_operators_in_properties"]) | set(u["operators_in_filters"]))
        invs = [u]
    for i in invs:
        print(json.dumps(i, indent=1, sort_keys=False))
