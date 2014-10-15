#! /usr/bin/env python
import os
import sys
import subprocess

# For ecn server to subprocess
def fn_check_output():
    def f(*popenargs, **kwargs):
        if 'stdout' in kwargs:
            raise ValueError('stdout argument not allowed, it will be overridden.')
        process = subprocess.Popen(stdout=subprocess.PIPE, *popenargs, **kwargs)
        output, unused_err = process.communicate()
        retcode = process.poll()
        if retcode:
            cmd = kwargs.get("args")
            if cmd is None:
                cmd = popenargs[0]
            raise subprocess.CalledProcessError(retcode, cmd)
        return output
    return f

if "check_output" not in dir(subprocess):
    subprocess.check_output = fn_check_output()

errors = False
testdir = './test/'
for fname in os.listdir(testdir):
    if(fname.endswith('.micro') and (len(sys.argv) == 1 or fname.startswith(sys.argv[1]))):
        micro = testdir + fname
        myout = testdir + fname.replace('.micro', '.myout')
        trout = testdir + fname.replace('.micro', '.out')
        mout = testdir + fname.replace('.micro', '.m.myout')
        tout = testdir + fname.replace('.micro', '.t.out')

        connector = ";"
        if os.name == "posix": connector = ":"
        excommand = 'java -ea -cp lib/antlr.jar' + connector + 'classes/ Micro '+ micro + ' > ' + myout
        t1xcommand = testdir + 'tinyR ' + myout + ' > ' + mout
        t2xcommand = testdir + 'tinyR ' + trout + ' > ' + tout
        dfcommand = 'diff -y ' + mout + ' ' + tout

        print "Testing file:", fname
        try:
            print(excommand)
            subprocess.check_output(excommand, shell=True);
            print(t1xcommand)
            subprocess.check_output(t1xcommand, shell=True);
            print(t2xcommand)
            subprocess.check_output(t2xcommand, shell=True);
        except:
            print "--- Run time error"
            exit(1)

        os.system(dfcommand)

if(errors): exit(1)

