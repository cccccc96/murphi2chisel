import os,sys, argparse

FILE = ""
FILE_NAME = ""
DEFAULT_OUT = "out"
CONST_LIST = []

## python3 murphi2chisel/murphi2chisel.py mutual.m out
#   cp out/mutual.scala ChiselFV/src/main/scala/mutual.scala
# 生成对应的genMurphi
# cd ChiselFV/
# sbt "runMain genmutual 2"
# cd ..


header = """
murphi2chisel & veerify
"""

def getopts():

    p = argparse.ArgumentParser(description=str(header),formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument('file',help='top file',type=str)
    p.add_argument('-c','--constlist',help='constlist',nargs='+')

    p.add_argument('-o','--out',help='<output-path>',type=str,)

    args, leftovers = p.parse_known_args()
    return args, p.parse_args()

def setup():
    global FILE
    global DEFAULT_OUT
    global CONST_LIST
    global FILE_NAME
    known, opts = getopts()
    FILE = opts.file 
    FILE_NAME = FILE.split('/')[-1].replace('.m','')
    if opts is not None:
        DEFAULT_OUT = opts.out 
    CONST_LIST = opts.constlist

def genChisel2VerilogScala():
    global FILE
    global DEFAULT_OUT
    global CONST_LIST
    global FILE_NAME
    if len(CONST_LIST)==1:
        scala = """
        import chisel3._
        import chiselsby.{Check, Formal}
        object gen%s extends App {
            Check.generateRTL(() => new %s(args(0).toInt))
        }
        """ % (FILE_NAME, FILE_NAME)
    else:
        scala = """
        import chisel3._
        import chiselsby.{Check, Formal}
        object gen%s extends App {
            Check.generateRTL(() => new %s(args(0).toInt,args(1).toInt))
        }
        """ % (FILE_NAME, FILE_NAME)
    f = open('ChiselFV/src/main/scala/gen'+FILE_NAME+'.scala','w')
    f.write(scala)
    f.close()

def genChisel():
    global FILE
    global DEFAULT_OUT
    global CONST_LIST
    global FILE_NAME

    os.system('python3 murphi2chisel/murphi2chisel.py %s %s'% (FILE_NAME+'.m',DEFAULT_OUT))

def chisel2verilog():
    global FILE
    global DEFAULT_OUT
    global CONST_LIST
    global FILE_NAME
    genChisel2VerilogScala()
    os.system('cp %s %s' %(DEFAULT_OUT+'/'+FILE_NAME+'.scala','ChiselFV/src/main/scala/'+FILE_NAME+'.scala'))
    os.chdir('ChiselFV')
    print(2,os.getcwd())
    os.system('sbt \"runMain gen%s %s\"' % (FILE_NAME,' '.join(CONST_LIST)))
    os.chdir('..')
    print(2,os.getcwd())
    os.system('rm ' +'ChiselFV/src/main/scala/gen'+FILE_NAME+'.scala')
    os.system('rm ' +'ChiselFV/src/main/scala/'+FILE_NAME+'.scala')

def updateverilog():
    global FILE
    global DEFAULT_OUT
    global CONST_LIST
    global FILE_NAME
    f1= open('ChiselFV/%s_build/%s.sv' % (FILE_NAME,FILE_NAME))
    f2 = open('%s/%s.sv' % (DEFAULT_OUT,FILE_NAME), 'w')
    sv = ""
    flag = False
    for line in f1:
        if "`ifndef SYNTHESIS" in line:
            flag=True
        if flag==False:
            sv+= line
        if "`endif // SYNTHESIS" in line:
            flag=False
        if "initial begin" in line:
            sv +="""
            initial begin
                assume(reset==1);
            end\n
            """
    f2.write(sv)
    f1.close()
    f2.close()

def verilog2btor2():
    f = open(DEFAULT_OUT+'/synth.ys' , 'w')
    out = """
    read -formal ./%s.sv;
    prep -top %s;
    flatten;
    memory -nomap;
    hierarchy -check;
    setundef -undriven -expose;
    write_btor %s.btor2;
    """ % (FILE_NAME,FILE_NAME,FILE_NAME)
    f.write(out)
    f.close()
    os.chdir('out')
    os.system('yosys ./synth.ys')
    os.chdir('..')


def runavr():
    os.chdir('avr-master')
    os.system('python3 avr.py -o ../%s/log -n bar ../out/%s.btor2' % (DEFAULT_OUT,FILE_NAME))
    os.chdir('..')

def main():
    setup()
    genChisel()
    chisel2verilog()
    updateverilog()
    verilog2btor2()
    runavr()

if __name__ =='__main__':
    # python3 verify.py ./example/german.m -c 2 2 -o out 
    main()