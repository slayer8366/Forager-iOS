// Prints the line span of every class, method, constructor, initializer and field in a Java source tree, using
// the JDK's own parser (com.sun.source), for the BRouter port's reach count (dispatch 2026-10-28-01, item A).
//
//   java SourceSpans.java <source root>  > spans.tsv
//
// Columns: kind, file (relative to root), binary class name, member name, parameter types (erased, simple names,
// comma-separated), first line, last line, first line including the comment block directly above.
// kind: C class/interface/enum, M method or constructor (<init>), I initializer block (<clinit> if static), F field.
// Local and anonymous classes are not listed: their lines lie inside the method that declares them.
import com.sun.source.tree.*;
import com.sun.source.util.*;
import javax.tools.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

public class SourceSpans {
  public static void main(String[] args) throws Exception {
    Path root = Paths.get(args[0]).toAbsolutePath();
    List<Path> files;
    try (Stream<Path> s = Files.walk(root)) {
      files = s.filter(p -> p.toString().endsWith(".java")).sorted().collect(Collectors.toList());
    }
    JavaCompiler jc = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fm = jc.getStandardFileManager(null, null, StandardCharsets.UTF_8);
    JavacTask task = (JavacTask) jc.getTask(null, fm, null, List.of("-proc:none"), null, fm.getJavaFileObjectsFromPaths(files));
    SourcePositions sp = Trees.instance(task).getSourcePositions();
    for (CompilationUnitTree cu : task.parse()) {
      String rel = root.relativize(Paths.get(cu.getSourceFile().toUri())).toString();
      List<String> lines = Files.readAllLines(Paths.get(cu.getSourceFile().toUri()), StandardCharsets.UTF_8);
      String pkg = cu.getPackageName() == null ? "" : cu.getPackageName().toString() + ".";
      LineMap lm = cu.getLineMap();
      for (Tree t : cu.getTypeDecls()) {
        if (t instanceof ClassTree) visitClass(cu, (ClassTree) t, pkg, rel, lines, lm, sp);
      }
    }
  }

  static int line(LineMap lm, long pos) { return (int) lm.getLineNumber(pos); }

  /** The first line of the comment block directly above line [first] (1-based), or [first] if there is none. */
  static int withComment(List<String> lines, int first) {
    int i = first - 1; // index of the line above, 1-based
    int top = first;
    boolean inBlock = false;
    while (i >= 1) {
      String s = lines.get(i - 1).trim();
      if (inBlock) {
        top = i;
        if (s.startsWith("/*")) inBlock = false;
        i--;
        continue;
      }
      if (s.startsWith("//")) { top = i; i--; continue; }
      if (s.endsWith("*/")) {
        top = i;
        if (!s.startsWith("/*")) inBlock = true;
        i--;
        continue;
      }
      break;
    }
    return top;
  }

  static String erase(Tree type) {
    String s = type.toString();
    // drop generic arguments, keep array brackets
    StringBuilder b = new StringBuilder();
    int depth = 0;
    for (char c : s.toCharArray()) {
      if (c == '<') depth++;
      else if (c == '>') depth--;
      else if (depth == 0) b.append(c);
    }
    String r = b.toString().trim();
    int dot = r.lastIndexOf('.', r.indexOf('[') < 0 ? r.length() : r.indexOf('['));
    if (dot >= 0) r = r.substring(dot + 1);
    return r.replace("...", "[]").replace(" ", "");
  }

  static void visitClass(CompilationUnitTree cu, ClassTree ct, String binaryPrefix, String rel, List<String> lines,
                         LineMap lm, SourcePositions sp) {
    String bin = binaryPrefix + ct.getSimpleName();
    int s = line(lm, sp.getStartPosition(cu, ct)), e = line(lm, sp.getEndPosition(cu, ct));
    String kind = ct.getKind().toString().toLowerCase();
    System.out.println(String.join("\t", "C", rel, bin, kind, "", "" + s, "" + e, "" + withComment(lines, s)));
    for (Tree m : ct.getMembers()) {
      long st = sp.getStartPosition(cu, m), en = sp.getEndPosition(cu, m);
      if (st < 0 || en < 0) continue; // compiler-generated (e.g. default constructor): no source
      int ms = line(lm, st), me = line(lm, en);
      if (m instanceof MethodTree) {
        MethodTree mt = (MethodTree) m;
        String name = mt.getName().contentEquals("<init>") ? "<init>" : mt.getName().toString();
        String params = mt.getParameters().stream().map(p -> erase(p.getType())).collect(Collectors.joining(","));
        System.out.println(String.join("\t", "M", rel, bin, name, params, "" + ms, "" + me, "" + withComment(lines, ms)));
      } else if (m instanceof BlockTree) {
        String name = ((BlockTree) m).isStatic() ? "<clinit>" : "<init-block>";
        System.out.println(String.join("\t", "I", rel, bin, name, "", "" + ms, "" + me, "" + withComment(lines, ms)));
      } else if (m instanceof VariableTree) {
        System.out.println(String.join("\t", "F", rel, bin, ((VariableTree) m).getName().toString(), erase(((VariableTree) m).getType()), "" + ms, "" + me, "" + withComment(lines, ms)));
      } else if (m instanceof ClassTree) {
        visitClass(cu, (ClassTree) m, bin + "$", rel, lines, lm, sp);
      }
    }
  }
}
