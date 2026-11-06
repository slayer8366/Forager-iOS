# Reads the canEscape counter's counts.txt: per JVM (pid) its regime, case, calls and false answers.
import sys, re, collections
loaded={}; summ={}; firsts=collections.defaultdict(list)
for l in open(sys.argv[1]):
    kind,rest=l.split(' ',1)
    pid=re.search(r'pid=(\d+)',rest).group(1)
    cmd=rest.split(' cmd=',1)[1].strip()
    if kind=='loaded': loaded[pid]=cmd
    elif kind=='summary':
        m=re.search(r'calls=(\d+) false=(\d+)',rest); summ[pid]=(int(m.group(1)),int(m.group(2)),cmd)
    elif kind=='false': firsts[pid].append(re.search(r'node=(\S+)',rest).group(1))
def who(cmd):
    f=cmd.split()
    if f[0].endswith('E3Jvm'): return f[1], f[-1], ('select' if '-select/' in cmd else 'E3')
    return 'other',cmd[:60],'-'
print("JVMs loaded:",len(loaded),"with a summary:",len(summ),"loaded without summary:",[ (p,loaded[p][:80]) for p in loaded if p not in summ])
rows=collections.defaultdict(dict)
tot=collections.Counter()
for pid,(c,f,cmd) in summ.items():
    reg,case,phase=who(cmd)
    rows[case][(phase,reg)]=(c,f)
    tot[(phase,reg,'calls')]+=c; tot[(phase,reg,'false')]+=f
    tot[(phase,reg,'jvms')]+=1; tot[(phase,reg,'jvms-with-false')]+= (f>0)
for k in sorted(set((a,b) for a,b,_ in tot)):
    print("phase %s regime %s: JVMs %d, with false %d, calls %d, false %d"%(k[0],k[1],tot[k+('jvms',)],tot[k+('jvms-with-false',)],tot[k+('calls',)],tot[k+('false',)]))
cols=[('select','R1R2'),('E3','R1R2'),('E3','R4'),('E3','R5'),('E3','R6')]
if len(sys.argv)>2:
    with open(sys.argv[2],'w') as o:
        o.write("case\t"+"\t".join("%s-%s-calls\t%s-%s-false"%(a,b,a,b) for a,b in cols)+"\n")
        for case in sorted(rows):
            o.write(case+"\t"+"\t".join("%s\t%s"%rows[case].get(k,('-','-')) for k in cols)+"\n")
    print("per-case table written:",sys.argv[2])
