import os, shutil
from os import path
from subprocess import Popen, PIPE, STDOUT

# Dummy stub method for TODO purposes
def dummy(): return

# Returns current Git branch
def getCurrentBranch():
    args = 'git rev-parse --abbrev-ref HEAD'.split()
    p = Popen(args, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    preprocessed, _ = p.communicate()
    return str(preprocessed, 'utf-8').rsplit()[0]

# Commits changes. Required before switching branches.
def commitChanges(message):
    addall = 'git add .'
    pAdd = Popen(addall, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    addMsg, _ = pAdd.communicate()
    print('Adding all: {}'.format(str(addMsg, 'utf-8')))
    commit = 'git commit -m \"Run test\"'
    pCommit = Popen(commit, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    commitMsg, _ = pCommit.communicate()
    print('Committing: {}'.format(str(commitMsg, 'utf-8')))
    return pCommit.returncode

# Switches branches. If assigned to a var, returns the exit code
def switchBranch(branch):
    args = ['git', 'checkout', branch]
    p = Popen(args, stdin=PIPE, stdout=PIPE, stderr=STDOUT)
    sdata, _ = p.communicate()
    print(str(sdata, 'utf-8'))
    return p.returncode

# MAIN FUNCTION

def main():
    print('\n=> Attempting to copy world files from master to worldfiles')
    
    # ensure that current branch is master
    print('=> Checking to make sure we\'re in master')
    currentBranch = getCurrentBranch()

    if not currentBranch == 'master':
        print('==> Current branch: {} -- switching to master'.format(currentBranch))
        
        returncode = switchBranch('master')
        if not returncode == 0:
            print('==> Ensure that your branch is fully up to date before copying the files')
            return
    
    # walk through ./server/ and copy files
    print('\n=> Copying files to temp folder...')
    for root, dirs, files in os.walk('.\server\\'):
        for dir in dirs:
            if dir.startswith('world'):
                
                src = path.join(root, dir)
                dst = '..\.tmpworlds_{}'.format(dir)
                print('==> Copying directory from {} to {}'.format(dir, dst))
                
                try:    
                    shutil.copytree(src, dst)
                except FileExistsError:
                    print('===> Destination exists; removing it')
                    shutil.rmtree(dst)
                    shutil.copytree(src, dst)
    
    print('=> Done')
    
    # commit and switch branch
    print('==> Switching to branch \'worldfiles\'')
    commitcode = commitChanges('Run test')
    if not commitcode == 0:
        print('==> *** Failed to commit! ***')
    returncode = switchBranch('worldfiles')
    if not returncode == 0:
        print('==> Ensure that your branch is fully up to date before copying the files')
        return
    
    # clean up server folder
    print('\n=> Clearing server folder...')
    for root, dirs, files in os.walk('.\server\\', topdown=False):
        for file in files:
            where = path.join(root, file)
            os.remove(where)
            print('==> Deleted file {}'.format(where))
        for dir in dirs:
            where = path.join(root, dir)
            os.rmdir(where)
            print('==> Removed directory {}'.format(where))
    print('=> Done')
    
    # copy files back
    print('\n=> Copying files to worldfiles branch...')
    for root, dirs, files in os.walk('..'):
        for dir in dirs:
            if dir.startswith('.tmpworlds'):
                
                src = path.join(root, dir)
                dst = '.\server\\{}'.format(dir.split('_', 1)[1])
                print('==> Copying directory from {} to {}'.format(dir, dst))
                
                try:
                    shutil.copytree(src, dst)
                except FileExistsError:
                    print('===> Destination exists; removing it')
                    shutil.rmtree(dst)
                    shutil.copytree(src, dst)
    print('=> Done')
    
    # clean up temp folders
    print('\n=> Cleaning up...')
    for root, dirs, files in os.walk('..'):
        for dir in dirs:
            if dir.startswith('.tmpworlds'):
                targ = path.join(root, dir)
                print('==> Removing temp directory {}'.format(dir))
                shutil.rmtree(targ)
    print('=> Done')
    
    # commit changes to worldfiles branch
    print('\n=> Committing changes before returning to master')
    returncode = commitChanges('Update world files')
    if not returncode == 0:
        print('==> *** Failed to commit changes!')
    
    # return to master branch
    print('\n=> Returning to master branch')
    returncode = switchBranch('master')
    if not returncode == 0:
        print('==> Failed to return to master branch. \
        ***You are still in branch {}***'.format(getCurrentBranch()))

###########
# EXECUTE #
###########
main()
print('Done')
