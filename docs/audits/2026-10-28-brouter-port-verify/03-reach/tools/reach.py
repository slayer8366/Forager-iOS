#!/usr/bin/env python3
"""Method-level reach of BRouter v1.7.10 from Forager's calls (dispatch 2026-10-28-01, item A).

Rapid type analysis over `javap -v -p` output of the vendored source compiled with javac --release 11:
- a method is reached if an entry point calls it, or a reached method calls it;
- a virtual or interface call reaches the override in every class that some reached code instantiates
  (or that Forager instantiates), never in a class nothing creates;
- a class's static initializer is reached when reached code creates it, reads or writes one of its static
  fields, or calls one of its static methods (and its superclasses' with it);
- a method that overrides a JDK method (toString, equals, hashCode, compareTo, compare, run, ...) in an
  instantiated class is reached as a "JDK callback", because the JDK may call it; these are listed apart;
- excluded edges (exclusions file) are cut, each with the source lines that justify it.

  python3 -I reach.py <javap.txt> <spans.tsv> <entries.txt> <exclusions.txt> <jdk javap cache dir> <out dir>

Writes to <out dir>: reached-methods.tsv, port-list.tsv, summary.txt, edges.tsv.
"""
import os
import re
import subprocess
import sys
from collections import defaultdict

javap_path, spans_path, entries_path, excl_path, jdk_cache, out_dir = sys.argv[1:7]

# ---------------------------------------------------------------------------------------------- parse javap

def strip_generics(s):
    out, depth = [], 0
    for c in s:
        if c == '<':
            depth += 1
        elif c == '>':
            depth -= 1
        elif depth == 0:
            out.append(c)
    return ''.join(out)


DECL_RE = re.compile(r'^(?:[a-z]+ )*(class|interface) ([\w.$]+)(?: extends ([\w.$, ]+?))?(?: implements ([\w.$, ]+))?\s*$')
INSN_RE = re.compile(r'^\s+(\d+): (\w+)\s*(?:#\d+(?:,\s*\d+)?)?\s*(?://\s*(\w+) (.*))?$')
LINE_RE = re.compile(r'^\s+line (\d+): (\d+)$')


def parse_decl(line):
    m = DECL_RE.match(strip_generics(line).strip())
    if not m:
        return None
    kind, name, ext, impl = m.groups()
    name = name.replace('.', '/')
    supers = [s.strip().replace('.', '/') for s in ext.split(',')] if ext else []
    ifaces = [s.strip().replace('.', '/') for s in impl.split(',')] if impl else []
    if kind == 'interface':
        return name, True, None, supers  # an interface's "extends" lists interfaces
    return name, False, (supers[0] if supers else 'java/lang/Object'), ifaces


def method_name_from_decl(decl, cls):
    d = decl.strip().rstrip(';')
    if d == 'static {}':
        return '<clinit>'
    head = d.split('(')[0].split()
    nm = head[-1] if head else ''
    if nm == cls:
        return '<init>'
    return nm


def parse_ref(owner_default, text):
    """'btools/x/Y."<init>":(I)V' or 'foo:(I)V' -> (owner, name, desc)."""
    text = text.strip()
    m = re.match(r'^(?:"?([^":]+?)"?\.)?"?([^":.]+)"?:(.*)$', text)
    if not m:
        return None
    owner, name, desc = m.groups()
    return (owner or owner_default), name, desc


classes = {}


def parse_javap(path, store, is_jdk=False):
    cur = None
    meth = None
    in_code = False
    in_lines = False
    pending_decl = None
    with open(path, encoding='utf-8') as f:
        for raw in f:
            line = raw.rstrip('\n')
            if line.startswith('Classfile '):
                cur = None
                meth = None
                continue
            if line.startswith('  Compiled from '):
                src = line.split('"')[1]
                pending_src = src
                continue
            if cur is None:
                d = parse_decl(line) if re.match(r'^(?:[a-z]+ )*(class|interface) ', line) else None
                if d:
                    name, is_iface, sup, ifaces = d
                    cur = {'name': name, 'iface': is_iface, 'super': sup, 'ifaces': ifaces, 'methods': {},
                           'fields': {}, 'src': None if is_jdk else pending_src, 'flags': ''}
                    store[name] = cur
                continue
            if line.startswith('  flags: ') and meth is None and not cur['methods'] and not cur['fields']:
                cur['flags'] = line
                continue
            # member declaration: two-space indent, not a key: value line
            if re.match(r'^  [^ #{}]', line) and not re.match(r'^  (minor|major|flags|this_class|super_class|interfaces|Constant pool|SourceFile|InnerClasses|Signature|NestMembers|NestHost|BootstrapMethods|Compiled|Last|SHA|MD5|Module|Record|PermittedSubclasses|RuntimeVisible|RuntimeInvisible|EnclosingMethod|Deprecated)', line):
                pending_decl = line
                meth = None
                in_code = in_lines = False
                continue
            if line.startswith('    descriptor: ') and pending_decl is not None:
                desc = line.split(': ', 1)[1].strip()
                if '(' in pending_decl or pending_decl.strip() == 'static {};':
                    nm = method_name_from_decl(pending_decl, cur['name'].replace('/', '.'))
                    meth = {'name': nm, 'desc': desc, 'flags': '', 'insns': [], 'lines': [], 'decl': pending_decl.strip()}
                    cur['methods'][(nm, desc)] = meth
                else:
                    fname = pending_decl.strip().rstrip(';').split()[-1]
                    cur['fields'][fname] = {'desc': desc, 'decl': pending_decl.strip()}
                    meth = None
                pending_decl = None
                continue
            if meth is None:
                continue
            if line.startswith('    flags: '):
                meth['flags'] = line
                continue
            if line.startswith('    Code:'):
                in_code = True
                continue
            if line.startswith('      LineNumberTable:'):
                in_lines = True
                in_code = False
                continue
            if line.startswith('      LocalVariableTable:') or line.startswith('      StackMapTable:') or \
                    line.startswith('      LocalVariableTypeTable:') or line.startswith('      Exception table:') or \
                    line.startswith('    Exceptions:') or line.startswith('    Signature:') or \
                    line.startswith('    RuntimeVisible') or line.startswith('    MethodParameters:'):
                in_lines = False
                if not line.startswith('      Exception table:'):
                    in_code = False
                continue
            if in_lines:
                m = LINE_RE.match(line)
                if m:
                    meth['lines'].append(int(m.group(1)))
                continue
            if in_code:
                m = INSN_RE.match(line)
                if m:
                    pc, op, kind, rest = m.groups()
                    meth['insns'].append((op, kind, rest))


parse_javap(javap_path, classes)
BR = set(classes)  # BRouter classes

# ---------------------------------------------------------------------------------------------- JDK supertypes

jdk = {}


def jdk_class(name):
    if name in jdk:
        return jdk[name]
    os.makedirs(jdk_cache, exist_ok=True)
    p = os.path.join(jdk_cache, name.replace('/', '.') + '.txt')
    if not os.path.exists(p):
        out = subprocess.run(['javap', '-v', '-p', name.replace('/', '.')], capture_output=True, text=True).stdout
        with open(p, 'w', encoding='utf-8') as f:
            f.write(out)
    store = {}
    parse_javap(p, store, is_jdk=True)
    c = store.get(name)
    if c is None:
        c = {'name': name, 'iface': False, 'super': None if name == 'java/lang/Object' else 'java/lang/Object',
             'ifaces': [], 'methods': {}, 'fields': {}, 'src': None, 'flags': ''}
    if name == 'java/lang/Object':
        c['super'] = None
    jdk[name] = c
    return c


def cls(name):
    if name in classes:
        return classes[name]
    if name.startswith('['):
        return None
    return jdk_class(name)


ancestors_cache = {}


def ancestors(name):
    """All supertypes of name, including itself (classes and interfaces, BRouter and JDK)."""
    if name in ancestors_cache:
        return ancestors_cache[name]
    res = set()
    stack = [name]
    while stack:
        n = stack.pop()
        if n in res or n is None:
            continue
        res.add(n)
        c = cls(n)
        if c is None:
            continue
        if c['super']:
            stack.append(c['super'])
        stack.extend(c['ifaces'])
    ancestors_cache[name] = res
    return res


def superchain(name):
    while name:
        yield name
        c = cls(name)
        name = c['super'] if c else None


def is_abstract(m):
    return 'ACC_ABSTRACT' in m['flags']


def is_static(m):
    return 'ACC_STATIC' in m['flags']


def is_private(m):
    return 'ACC_PRIVATE' in m['flags']


def resolve_decl(owner, name, desc):
    """The method a reference names: the first declaration up the superclass chain, then interfaces."""
    for c in superchain(owner):
        cc = cls(c)
        if cc and (name, desc) in cc['methods']:
            return c
    for a in ancestors(owner):
        cc = cls(a)
        if cc and (name, desc) in cc['methods']:
            return a
    return None


def dispatch(t, name, desc):
    """The method a virtual call runs on an object of class t."""
    for c in superchain(t):
        cc = cls(c)
        if cc and (name, desc) in cc['methods'] and not is_abstract(cc['methods'][(name, desc)]):
            return c
    for a in ancestors(t):  # default methods
        cc = cls(a)
        if cc and cc['iface'] and (name, desc) in cc['methods'] and not is_abstract(cc['methods'][(name, desc)]):
            return a
    return None

# ---------------------------------------------------------------------------------------------- inputs

entries = []          # (owner, name, desc)
seed_instantiated = []
field_entries = []
with open(entries_path, encoding='utf-8') as f:
    for line in f:
        line = line.split('#', 1)[0].strip()
        if not line:
            continue
        kind, rest = line.split(None, 1)
        if kind == 'call':
            entries.append(parse_ref(None, rest))
        elif kind == 'new':
            seed_instantiated.append(rest.strip())
        elif kind == 'field':
            field_entries.append(rest.strip())

excluded_edges = {}   # (caller key, callee key) -> reason
excluded_methods = {}  # callee key -> reason (all edges into it, and as a JDK callback)
# Exclusion lines: "TIER | caller -> callee | reason" or "TIER | method | reason". Only the tiers named in the
# TIERS environment variable (comma-separated, e.g. "F,E,S") are applied.
tiers = {t for t in os.environ.get('TIERS', '').split(',') if t}
with open(excl_path, encoding='utf-8') as f:
    for line in f:
        if not line.strip() or line.lstrip().startswith('#'):
            continue
        parts = [p.strip() for p in line.split('|', 2)]
        if len(parts) != 3:
            sys.exit(f'bad exclusion line: {line}')
        tier, body, reason = parts
        if tier not in tiers:
            continue
        ends = body.split('->')
        if len(ends) == 2:
            excluded_edges[(ends[0].strip(), ends[1].strip())] = tier + ': ' + reason
        else:
            excluded_methods[ends[0].strip()] = tier + ': ' + reason


def key(owner, name, desc):
    return f'{owner}.{name}{desc}'

# ---------------------------------------------------------------------------------------------- RTA

reached = {}            # key -> reason (first)
reached_meta = {}       # key -> (owner, name, desc)
instantiated = set()
initialized = set()
virtual_sites = []      # (caller key, owner, name, desc)
edges = []              # (caller, callee, kind)
used_excl_edges = set()
used_excl_methods = set()
work = []
fields_read = set()


def reach(owner, name, desc, why, caller=None, kind='call'):
    if owner not in BR:
        return
    k = key(owner, name, desc)
    if caller is not None:
        if (caller, k) in excluded_edges:
            used_excl_edges.add((caller, k))
            return
        if k in excluded_methods:
            used_excl_methods.add(k)
            return
        edges.append((caller, k, kind))
    if k in reached:
        return
    reached[k] = why
    reached_meta[k] = (owner, name, desc)
    work.append(k)


def initialize(c, caller):
    for s in superchain(c):
        if s not in BR or s in initialized:
            continue
        initialized.add(s)
        if ('<clinit>', '()V') in classes[s]['methods']:
            reach(s, '<clinit>', '()V', 'class init', caller, 'clinit')


def instantiate(c, caller):
    if c not in BR or c in instantiated:
        return
    instantiated.add(c)
    initialize(c, caller)
    for (ck, o, n, d) in list(virtual_sites):
        if o in ancestors(c):
            t = dispatch(c, n, d)
            if t:
                reach(t, n, d, 'virtual', ck, 'virtual')
    # JDK callbacks: overrides of JDK-declared methods
    for (n, d), m in classes[c]['methods'].items():
        if n in ('<init>', '<clinit>') or is_static(m) or is_private(m) or is_abstract(m):
            continue
        for a in ancestors(c):
            if a in BR:
                continue
            ac = cls(a)
            if ac and (n, d) in ac['methods']:
                reach(c, n, d, 'jdk-callback:' + a, 'JDK', 'jdk-callback')
                break
    # inherited BRouter methods overriding JDK methods (e.g. a superclass's toString) are covered when that
    # superclass's own methods are considered for the instantiated subclass:
    for s in superchain(c):
        if s == c or s not in BR:
            continue
        for (n, d), m in classes[s]['methods'].items():
            if n in ('<init>', '<clinit>') or is_static(m) or is_private(m) or is_abstract(m):
                continue
            if dispatch(c, n, d) != s:
                continue
            for a in ancestors(s):
                if a in BR:
                    continue
                ac = cls(a)
                if ac and (n, d) in ac['methods']:
                    reach(s, n, d, 'jdk-callback:' + a, 'JDK', 'jdk-callback')
                    break


for c in seed_instantiated:
    instantiate(c, 'FORAGER')
for (o, n, d) in entries:
    t = resolve_decl(o, n, d)
    if t is None:
        sys.exit(f'entry not found: {o}.{n}{d}')
    reach(t, n, d, 'entry', 'FORAGER', 'entry')
    if not is_static(classes[t]['methods'][(n, d)]) and n != '<init>':
        virtual_sites.append(('FORAGER', o, n, d))
        for c in list(instantiated):
            if o in ancestors(c):
                tt = dispatch(c, n, d)
                if tt:
                    reach(tt, n, d, 'virtual', 'FORAGER', 'virtual')
for fe in field_entries:
    fields_read.add(fe)

concat_object_args = set()

while work:
    k = work.pop()
    owner, name, desc = reached_meta[k]
    m = classes[owner]['methods'][(name, desc)]
    newed = set()  # classes this method creates with `new`; instantiated when their constructor call is not cut
    for (op, kind, rest) in m['insns']:
        if kind in ('Method', 'InterfaceMethod'):
            ref = parse_ref(owner, rest)
            if ref is None:
                continue
            o, n, d = ref
            if o.startswith('['):
                continue
            if op == 'invokestatic':
                initialize(o, k) if o in BR else None
                t = resolve_decl(o, n, d)
                if t:
                    reach(t, n, d, 'static', k, 'static')
            elif op == 'invokespecial':
                t = resolve_decl(o, n, d)
                if t:
                    reach(t, n, d, 'special', k, 'special')
                if n == '<init>' and o in newed:
                    ck = key(t or o, n, d)
                    if (k, ck) not in excluded_edges and ck not in excluded_methods:
                        instantiate(o, k)
            else:  # invokevirtual / invokeinterface
                # the named declaration is kept only as a signature (abstract); concrete code is reached only by
                # dispatch on an instantiated class, below
                t = resolve_decl(o, n, d)
                if t and t in BR and is_abstract(classes[t]['methods'][(n, d)]):
                    reach(t, n, d, 'declaration', k, 'declaration')
                virtual_sites.append((k, o, n, d))
                for c in list(instantiated):
                    if o in ancestors(c):
                        tt = dispatch(c, n, d)
                        if tt:
                            reach(tt, n, d, 'virtual', k, 'virtual')
        elif kind == 'Field':
            ref = parse_ref(owner, rest)
            if ref:
                o, n, d = ref
                fields_read.add(f'{o}.{n}')
                if op in ('getstatic', 'putstatic') and o in BR:
                    # the field may be declared in a superclass
                    initialize(o, k)
        elif kind == 'class':
            c = rest.strip().strip('"')
            if op == 'new':
                if c in BR:
                    newed.add(c)
        elif kind == 'InvokeDynamic':
            # string concatenation: the recipe's argument types say whose toString() runs
            mm = re.search(r'makeConcatWithConstants:\(([^)]*)\)', rest)
            if mm:
                for t in re.findall(r'L([^;]+);', mm.group(1)):
                    concat_object_args.add(t)
                    virtual_sites.append((k, t, 'toString', '()Ljava/lang/String;'))
                    for c in list(instantiated):
                        if t in ancestors(c):
                            tt = dispatch(c, 'toString', '()Ljava/lang/String;')
                            if tt:
                                reach(tt, 'toString', '()Ljava/lang/String;', 'virtual', k, 'concat')

# ---------------------------------------------------------------------------------------------- source spans

spans = []  # (kind, file, cls, name, params, s, e, sc)
with open(spans_path, encoding='utf-8') as f:
    for line in f:
        p = line.rstrip('\n').split('\t')
        spans.append((p[0], p[1], p[2].replace('.', '/'), p[3], p[4], int(p[5]), int(p[6]), int(p[7])))

file_lines = {}
src_root = os.environ.get('SRC_ROOT')
for sp in spans:
    if sp[1] not in file_lines:
        with open(os.path.join(src_root, sp[1]), encoding='utf-8') as f:
            file_lines[sp[1]] = sum(1 for _ in f)

PRIM = {'I': 'int', 'J': 'long', 'Z': 'boolean', 'B': 'byte', 'C': 'char', 'S': 'short', 'F': 'float', 'D': 'double', 'V': 'void'}


def desc_params(desc):
    inner = desc[1:desc.index(')')]
    out, i = [], 0
    while i < len(inner):
        dims = 0
        while inner[i] == '[':
            dims += 1
            i += 1
        if inner[i] == 'L':
            j = inner.index(';', i)
            t = inner[i + 1:j].split('/')[-1].split('$')[-1]
            i = j + 1
        else:
            t = PRIM[inner[i]]
            i += 1
        out.append(t + '[]' * dims)
    return out


def file_of_class(c):
    """Source file of a bytecode class: its outer class's span entry."""
    outer = c.split('$')[0]
    for sp in spans:
        if sp[0] == 'C' and sp[2] == outer:
            return sp[1]
    return None


method_spans = defaultdict(list)  # (cls) -> list of M/I spans
for sp in spans:
    if sp[0] in ('M', 'I'):
        method_spans[sp[2]].append(sp)


def source_member(c, name, desc, lines):
    """The source span (M or I) a bytecode method belongs to."""
    outer_named = c
    # anonymous/local classes (Outer$1): their methods' lines fall inside an enclosing member of the named class
    named = [x[2] for x in spans if x[0] == 'C']
    while outer_named not in named and '$' in outer_named:
        outer_named = outer_named.rsplit('$', 1)[0]
    cands = method_spans.get(outer_named, [])
    if outer_named != c:  # anonymous class: by line containment in the enclosing named class's members
        if lines:
            for sp in cands:
                if sp[5] <= min(lines) and max(lines) <= sp[6]:
                    return sp, 'enclosing'
        return None, 'anonymous-unmapped'
    want = desc_params(desc)
    # by name and parameter types
    byname = [sp for sp in cands if sp[3] == name and sp[0] == 'M']
    exact = [sp for sp in byname if [p for p in sp[4].split(',') if p] == want]
    if len(exact) == 1:
        return exact[0], 'signature'
    if lines:
        for sp in cands:
            if sp[5] <= min(lines) and max(lines) <= sp[6]:
                return sp, 'lines'
    if name == '<clinit>' or name == '<init>':
        return None, 'field-initializers'  # implicit: field initializers / default constructor
    generic = [sp for sp in byname if len([p for p in sp[4].split(',') if p]) == len(want)]
    if len(generic) == 1:
        return generic[0], 'arity'
    return None, 'unmapped'

# ---------------------------------------------------------------------------------------------- report

os.makedirs(out_dir, exist_ok=True)
reached_spans = {}
unmapped = []
with open(os.path.join(out_dir, 'reached-methods.tsv'), 'w', encoding='utf-8') as f:
    f.write('class\tmethod\tdescriptor\treason\tsource\tfirst\tlast\tmapped-by\n')
    for k in sorted(reached):
        o, n, d = reached_meta[k]
        m = classes[o]['methods'][(n, d)]
        sp, how = source_member(o, n, d, m['lines'])
        if sp:
            reached_spans[(sp[1], sp[2], sp[3], sp[4], sp[5])] = sp
            f.write(f'{o}\t{n}\t{d}\t{reached[k]}\t{sp[1]}\t{sp[5]}\t{sp[6]}\t{how}\n')
        else:
            if how not in ('field-initializers',) and not ('ACC_SYNTHETIC' in m['flags'] or 'ACC_BRIDGE' in m['flags']):
                unmapped.append((k, how))
            f.write(f'{o}\t{n}\t{d}\t{reached[k]}\t\t\t\t{how}\n')

with open(os.path.join(out_dir, 'edges.tsv'), 'w', encoding='utf-8') as f:
    for (a, b, kd) in edges:
        f.write(f'{a}\t{b}\t{kd}\n')

# classes kept: a named class is kept if any member is reached, it is instantiated, initialized, or referenced
# as a type by reached code (descriptors, checkcast, instanceof, field types), or is a supertype of a kept class
referenced = set()
for k in reached:
    o, n, d = reached_meta[k]
    referenced.add(o)
    for t in re.findall(r'L([^;]+);', d):
        referenced.add(t)
    for (op, kind, rest) in classes[o]['methods'][(n, d)]['insns']:
        if kind == 'class':
            c = rest.strip().strip('"')
            for t in re.findall(r'L([^;]+);', c) or [c]:
                referenced.add(t)
        elif kind in ('Method', 'InterfaceMethod', 'Field'):
            ref = parse_ref(o, rest)
            if ref:
                referenced.add(ref[0])
                for t in re.findall(r'L([^;]+);', ref[2]):
                    referenced.add(t)
referenced |= instantiated | initialized
kept_classes = set()
for c in referenced:
    if c in BR:
        kept_classes |= {a for a in ancestors(c) if a in BR}
kept_named = set()
named = {x[2] for x in spans if x[0] == 'C'}
for c in kept_classes:
    n = c
    while n not in named and '$' in n:
        n = n.rsplit('$', 1)[0]
    kept_named.add(n)
    # a nested class's enclosing classes are kept too
    while '$' in n:
        n = n.rsplit('$', 1)[0]
        kept_named.add(n)

# lines: per file, file lines minus the spans (with their comment block) of members not reached and of named
# classes not kept. Fields are kept whatever their use.
rows = []
kind_counts = {'blank': 0, 'comment': 0, 'code': 0}
tot_file = tot_kept = 0
files_with_kept = set()
for fpath in sorted(file_lines):
    L = file_lines[fpath]
    tot_file += L
    removed = []
    fclasses = [sp for sp in spans if sp[0] == 'C' and sp[1] == fpath]
    top = [sp for sp in fclasses if '$' not in sp[2]]
    for sp in fclasses:
        if sp[2] not in kept_named:
            removed.append((sp[7], sp[6], 'class ' + sp[2]))
    for sp in spans:
        if sp[1] != fpath or sp[0] not in ('M', 'I'):
            continue
        if sp[2] not in kept_named:
            continue
        if (sp[1], sp[2], sp[3], sp[4], sp[5]) in reached_spans:
            continue
        if sp[0] == 'I':
            continue  # initializer blocks: kept with their class
        removed.append((sp[7], sp[6], sp[3] + '(' + sp[4] + ')'))
    # merge overlapping ranges (a removed nested class contains its members)
    removed.sort()
    merged = []
    for s, e, w in removed:
        if merged and s <= merged[-1][1]:
            merged[-1] = (merged[-1][0], max(merged[-1][1], e), merged[-1][2] + [w])
        else:
            merged.append((s, e, [w]))
    if all(sp[2] not in kept_named for sp in top):
        kept = 0
    else:
        kept = L - sum(e - s + 1 for s, e, _ in merged)
        files_with_kept.add(fpath)
        # classify the kept lines: blank, comment-only, code
        with open(os.path.join(src_root, fpath), encoding='utf-8') as sf:
            src_lines = sf.read().split('\n')[:L]
        cut = set()
        for s0, e0, _ in merged:
            cut.update(range(s0, e0 + 1))
        in_block = False
        for i, text in enumerate(src_lines, start=1):
            t = text.strip()
            if i in cut:
                if '/*' in t and '*/' not in t.split('/*', 1)[1]:
                    in_block = True
                if '*/' in t:
                    in_block = False
                continue
            if in_block:
                kind_counts['comment'] += 1
                if '*/' in t:
                    in_block = False
            elif not t:
                kind_counts['blank'] += 1
            elif t.startswith('//'):
                kind_counts['comment'] += 1
            elif t.startswith('/*'):
                kind_counts['comment'] += 1
                if '*/' not in t[2:]:
                    in_block = True
            else:
                kind_counts['code'] += 1
    tot_kept += kept
    kept_methods = sorted({sp[3] + '(' + sp[4] + ')' for sp in reached_spans.values() if sp[1] == fpath and sp[0] == 'M'})
    all_methods = [sp for sp in spans if sp[1] == fpath and sp[0] == 'M']
    rows.append((fpath, L, kept, len(kept_methods), len(all_methods), kept_methods))

with open(os.path.join(out_dir, 'port-list.tsv'), 'w', encoding='utf-8') as f:
    f.write('file\tfile_lines\tkept_lines\tmethods_kept\tmethods_in_file\tkept_methods\n')
    for r in rows:
        f.write(f'{r[0]}\t{r[1]}\t{r[2]}\t{r[3]}\t{r[4]}\t{" ".join(r[5])}\n')

with open(os.path.join(out_dir, 'summary.txt'), 'w', encoding='utf-8') as f:
    def w(s=''):
        f.write(s + '\n')
    w(f'bytecode classes parsed: {len(BR)}')
    w(f'source files: {len(file_lines)}, lines: {tot_file}')
    w(f'methods in bytecode: {sum(len(c["methods"]) for c in classes.values())}')
    w(f'methods reached: {len(reached)} (JDK callbacks among them: {sum(1 for v in reached.values() if v.startswith("jdk-callback"))})')
    w(f'classes instantiated: {len(instantiated)}; initialized: {len(initialized)}')
    w(f'named classes kept: {len(kept_named)} of {len(named)}')
    w(f'files with anything kept: {len(files_with_kept)} of {len(file_lines)}; their lines: {sum(file_lines[x] for x in files_with_kept)}')
    w(f'kept lines (files minus unreached methods and classes): {tot_kept} of {tot_file}')
    w(f'  of which blank {kind_counts["blank"]}, comment-only {kind_counts["comment"]}, code {kind_counts["code"]} (sum {sum(kind_counts.values())})')
    w(f'excluded edges used: {len(used_excl_edges)} of {len(excluded_edges)}; excluded methods used: {len(used_excl_methods)} of {len(excluded_methods)}')
    for e in sorted(set(excluded_edges) - used_excl_edges):
        w(f'  UNUSED exclusion edge: {e[0]} -> {e[1]}')
    for e in sorted(set(excluded_methods) - used_excl_methods):
        w(f'  UNUSED exclusion method: {e}')
    w(f'unmapped reached methods (no source span found): {len(unmapped)}')
    for k, how in unmapped:
        w(f'  {k} ({how})')
    w('JDK callbacks reached:')
    for k in sorted(k for k, v in reached.items() if v.startswith('jdk-callback')):
        w(f'  {k}  <- {reached[k]}')
    w('concat argument types (toString dispatch): ' + ' '.join(sorted(concat_object_args)))
    # calls through which the JDK could run a BRouter toString(): an Object-typed string conversion
    obj_str = re.compile(r'(append:\(Ljava/lang/Object;\)|valueOf:\(Ljava/lang/Object;\)|print(ln)?:\(Ljava/lang/Object;\)|'
                         r'format:\(Ljava/lang/String;\[Ljava/lang/Object;\)|java/lang/Object\.toString|java/util/[A-Za-z$]+\.toString)')
    hits = []
    for k in sorted(reached):
        o, n, d = reached_meta[k]
        for (op, kind, rest) in classes[o]['methods'][(n, d)]['insns']:
            if kind in ('Method', 'InterfaceMethod') and obj_str.search(rest):
                hits.append(f'  {k}: {op} {rest}')
    w(f'reached call sites that convert an Object to a string: {len(hits)}')
    for h in hits:
        w(h)
    w('instantiated: ' + ' '.join(sorted(instantiated)))
    w('files kept: ' + ' '.join(sorted(files_with_kept)))
    w('files not kept: ' + ' '.join(sorted(set(file_lines) - files_with_kept)))
print(open(os.path.join(out_dir, 'summary.txt')).read())
