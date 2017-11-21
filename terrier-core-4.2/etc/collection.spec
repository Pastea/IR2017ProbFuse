#add the files to index





      ( EXPR )   ! EXPR   -not EXPR   EXPR1 -a EXPR2   EXPR1 -and EXPR2
      --version -xdev -ignore_readdir_race -noignore_readdir_race
      -cnewer FILE -ctime N -empty -false -fstype TYPE -gid N -group NAME
      -depth --help -maxdepth LEVELS -mindepth LEVELS -mount -noleaf
      -exec COMMAND ; -exec COMMAND {} + -ok COMMAND ;
      -execdir COMMAND ; -execdir COMMAND {} + -okdir COMMAND ;
      -fprint0 FILE -fprint FILE -ls -fls FILE -prune -quit
      -ilname PATTERN -iname PATTERN -inum N -iwholename PATTERN -iregex PATTERN
      -links N -lname PATTERN -mmin N -mtime N -name PATTERN -newer FILE
      -nouser -nogroup -path PATTERN -perm [-/]MODE -regex PATTERN
      -readable -writable -executable
      -used N -user NAME -xtype [bcdpfls]      -context CONTEXT
      -wholename PATTERN -size N[bcwkMG] -true -type [bcdpflsD] -uid N
      EXPR1 -o EXPR2   EXPR1 -or EXPR2   EXPR1 , EXPR2
Please see also the documentation at http://www.gnu.org/software/findutils/.
Usage: find [-H] [-L] [-P] [-Olevel] [-D debugopts] [path...] [expression]
Use '-D help' for a description of the options, or see find(1)
Valid arguments for -D:
You can report (and track progress on fixing) bugs in the "find"
actions: -delete -print0 -printf FORMAT -fprintf FILE FORMAT -print 
default path is the current directory; default expression is -print
exec, help, opt, rates, search, stat, time, tree
expression may consist of: operators, options, tests, and actions:
https://savannah.gnu.org/bugs/?group=findutils or, if
normal options (always true, specified before other expressions):
operators (decreasing precedence; -and is implicit where no others are given):
positional options (always true): -daystart -follow -regextype
program via the GNU findutils bug-reporting page at
tests (N can be +N or -N or N): -amin N -anewer FILE -atime N -cmin N
you have no web access, by sending email to <bug-findutils@gnu.org>.
