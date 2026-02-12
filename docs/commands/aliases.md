# Aliases

Create shorthand names for commands or command sequences.

## Synopsis

```
alias
alias name
alias name='value' [name='value' ...]
unalias name [name ...]
```

## Description

Aliases allow you to define short names that expand to longer commands. When you type an alias as the first word of a command, it is replaced with its value before the input is parsed. This means aliases can contain pipes, redirects, and multiple arguments.

## alias

### List all aliases

```
alias
```

Prints all defined aliases in alphabetical order, in the format `alias name='value'`.

### Show a specific alias

```
alias name
```

Prints the value of the specified alias. If the alias is not defined, an error is printed.

### Define an alias

```
alias name='value'
```

Creates or updates an alias. The value replaces the alias name when used as the first word of a command. Multiple aliases can be defined in one command:

```bash
alias ll='ls -la' gs='git status' gp='git push'
```

## unalias

```
unalias name [name ...]
```

Removes one or more aliases. If an alias does not exist, an error is printed for that name.

## Examples

### Simple alias

```bash
alias ll='ls -la'
ll
# equivalent to: ls -la
```

### Alias with additional arguments

```bash
alias ll='ls -la'
ll /tmp
# equivalent to: ls -la /tmp
```

### Alias with pipes

```bash
alias greplogs='cat /var/log/app.log | grep ERROR'
greplogs
# equivalent to: cat /var/log/app.log | grep ERROR
```

### Alias with redirects

```bash
alias save='echo done > /tmp/status.txt'
save
# equivalent to: echo done > /tmp/status.txt
```

### Overriding a command with itself

```bash
alias ls='ls --color=auto'
ls
# equivalent to: ls --color=auto
# The alias does not recurse — "ls" in the value is treated as the real command
```

### Removing aliases

```bash
alias ll='ls -la'
unalias ll
ll
# ll: command not found
```

## Persistence

Aliases defined in the current session are lost when the shell exits. To make aliases permanent, add them to your `~/.jshrc` file:

```bash
# ~/.jshrc
alias ll='ls -la'
alias gs='git status'
alias gp='git push'
```

These aliases will be loaded automatically on every shell startup.

## Notes

- Aliases are expanded only when used as the **first word** of a command. They are not expanded inside arguments.
- Alias expansion happens **before** input parsing, so alias values can contain pipes (`|`), redirects (`>`), and any other shell syntax.
- Recursive aliases are protected: if an alias expands to a command that starts with another alias, it will be expanded, but an alias will never expand itself twice (preventing infinite loops).
- Aliases take precedence over both built-in and external commands. If you define `alias echo='echo PREFIX:'`, typing `echo hello` will run `echo PREFIX: hello`.
