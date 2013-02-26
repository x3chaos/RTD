import os, sys, shutil, subprocess, argparse
from os import path
from subprocess import Popen

# set up args
parser = argparse.ArgumentParser()
parser.add_argument('-quiet', help='No verbose output will be printed.', action='store_true')
parser.add_argument('--serverloc', metavar='loc', help='The location of the server folder. \
                    This can either be absolute or relative to the workspace root.', default='.\server\\')
parser.add_argument('--pluginloc', metavar='loc', help='The name of the Plugins folder inside \
                    the server folder. This is not a filepath.', default='plugins')
parser.add_argument('--exportloc', metavar='loc', help='The location of the JAR exports \
                    from Eclipse.', default='.\jars\\')
parser.add_argument('--buildtype', metavar='type', help='The type of build to test.', \
                    default='beta')
parser.add_argument('-keepconfig', help='The plugin directory will not be cleared.', \
                    action='store_true')

# set arg variables for later use
default = os.getcwd()
args = parser.parse_args()

quiet = args.quiet
serverloc = args.serverloc
pluginloc = args.pluginloc
exportloc = args.exportloc
buildtype = args.buildtype
keepconfig = args.keepconfig

# define methods
def disp(message):
    if not quiet: print(message)

print('Setting up environment...')
# check input before executing
if not path.isdir(serverloc):
    print('FATAL: server folder not found at ' + serverloc)
    sys.exit(1)
if not path.isdir(path.join(serverloc, pluginloc)):
    print('FATAL: plugin folder not found at '.format(path.join(serverloc, pluginloc)))
    sys.exit(2)
if not path.isdir(exportloc):
    print('FATAL: export folder not found at ' + exportloc)
    sys.exit(3)
if not buildtype == 'stable' and not buildtype == 'beta':
    print('FATAL: unsupported build type ' + buildtype)
    sys.exit(4)
pluginsFromRoot = path.join(serverloc, pluginloc)
disp('=> Found plugin folder at ' + path.join(serverloc, pluginloc))

# delete all plugin data
disp('=> Emptying plugin folder')
for root, dirs, files in os.walk(pluginsFromRoot, False):
    for file in files:
        filepath = path.join(root, file)
        disp('==> Delete file ' + filepath)
        os.remove(filepath)
    if not keepconfig:
        for dir in dirs:
            dirpath = path.join(root, dir)
            disp('==> Remove dir ' + dirpath)
            os.rmdir(dirpath)
disp('=> Done')

# copy jar
disp('=> Copying {} jar file'.format(buildtype))
for root, dirs, files in os.walk(exportloc):
    for file in files:
        if file.endswith('{}.jar'.format(buildtype[:1])):
            jarpath = path.join(root, file)
            disp('==> Found jar at ' + jarpath)
            shutil.copy(jarpath, pluginsFromRoot)
            disp('==> Copied jar to ' + pluginsFromRoot)
            break
disp('=> Done')

# finish up
print('Done')
