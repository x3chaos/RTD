"""
        Info for those viewing this file on GitHub:
        
    This file refers to ".\server\", a directory that is
    intentionally ignored by Git. If you have forked the
    repository and wish to build and test the plugin on
    your own, just create the directory and place the
    CraftBukkit jarfile inside it. MAKE SURE that the jar
    is named correctly: "craftbukkit.jar". If you wish to
    use a different name, just edit "test.bat" accordingly.
    
"""

import os, glob, sys, shutil, argparse

# arg parser
parser = argparse.ArgumentParser()
parser.add_argument('--buildtype', metavar='TYPE', help='the desired build type (default: beta)', default='beta')
parser.add_argument('--jarloc', metavar='LOC', help='the directory containing the jarfile', default=".\jars\\")
parser.add_argument('--pluginloc', metavar='PLG', help="the test server's plugin directory", default=".\server\plugins\\")
parser.add_argument('--serverloc', metavar='SRV', help='the directory of the test server', default=".\server\\")
args = parser.parse_args()

# define vars
defloc = os.getcwd()
buildtype = args.buildtype
suffix = buildtype[:1]
jarloc = args.jarloc
plugloc = args.pluginloc
servloc = args.serverloc

# init
print('\n=> Initializing testing environment for {} build'.format(buildtype))

# empty plugin dir
print('\n=> Emptying plugin directory...')
for root, dirs, files in os.walk(plugloc):
    for dir in dirs:
        print('==> Removing tree {}'.format(dir))
        shutil.rmtree(dir)
    for file in files:
        print('==> Removing file {}'.format(file))
        os.remove(plugloc + file)
print('=> done')

# copy jar from jarloc to plugloc
print('\n=> Finding {} jar to copy...'.format(buildtype))
for root, dirs, files in os.walk(jarloc):
    for file in files:
        if (file.endswith(suffix + '.jar')):
            print('==> Copying {}...'.format(file))
            shutil.copy(jarloc + file, plugloc)
            break
print('=> done')

sys.exit(0)
