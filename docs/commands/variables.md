# Variables

Set, export, and remove shell variables.

## Synopsis

```
set name=value [name=value ...]
export name[=value] [name[=value] ...]
unset name [name ...]
```

## Description

JSH supports two types of variables:

- **Shell variables** — available only within the current shell session. Not inherited by child processes (e.g. commands like `grep`, `cat`, etc.).
- **Exported (environment) variables** — inherited by all child processes launched from the shell.

All system environment variables are available as exported variables from the start of the session.

Variable values can be referenced using `$name` syntax.

## set

```
set name=value
```

Creates or updates a variable. If the variable is already exported, it stays exported and its value is updated. Otherwise, a shell-only variable is created.

Multiple assignments can be specified in one command:

```bash
set foo=1 bar=hello baz=/tmp
```

## export

```
export name=value
```

Sets the variable and marks it as exported. If the variable was previously shell-only, it becomes exported with the new value.

```
export name
```

Marks an existing shell variable as exported without changing its value. If the variable does not exist, it is created as exported with an empty value.

Multiple arguments can be combined:

```bash
export PATH=/usr/bin EDITOR=vim LANG
```

## unset

```
unset name
```

Removes the variable completely, regardless of whether it is a shell variable or an exported variable.

Multiple variables can be removed at once:

```bash
unset foo bar baz
```

## Examples

### Setting and using variables

```bash
set greeting=hello
echo $greeting
# hello
```

### Exporting a variable for child processes

```bash
# Set a shell variable, then export it
set MY_APP_ENV=production
export MY_APP_ENV

# Or do both in one step
export MY_APP_ENV=production
```

### Configuring environment for a command

```bash
export JAVA_OPTS="-Xmx512m"
java -jar app.jar
# java inherits JAVA_OPTS from the shell environment
```

### Cleaning up variables

```bash
export SECRET=abc123
# ... use it ...
unset SECRET
# SECRET is no longer available in the shell or child processes
```

### Promoting shell variables to environment

```bash
set foo=1
set bar=2
set baz=3

# Export all three at once (values are preserved)
export foo bar baz
```

## Notes

- A variable exists in only one category at a time: either shell-only or exported, never both.
- Assigning a value to an already-exported variable (via `set name=value`) keeps it exported.
- `unset` removes the variable from both categories — there is no way to "unexport" a variable while keeping its value.
- Invalid assignments (no `=` or `=` at position 0, e.g. `=value`) are silently skipped. Other valid assignments in the same command are still processed.
