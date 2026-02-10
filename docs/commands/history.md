# history

Display or manipulate the command history list.

## Synopsis

```
history [N]
history -c
history -r [filename]
history -w [filename]
history -a [filename]
```

## Description

The `history` command provides access to the command history — a list of previously executed commands. By default, history is automatically loaded from and saved to a file between sessions.

## Options

| Option | Description |
|--------|-------------|
| `N` | Display only the last N entries |
| `-c` | Clear the history list (does not affect the history file) |
| `-r [filename]` | Read history from file and append to current session |
| `-w [filename]` | Write current history to file (overwrites existing content) |
| `-a [filename]` | Append new entries (commands executed since last write) to file |

If `filename` is omitted for `-r`, `-w`, or `-a`, the default history file is used.

## Examples

### Display history

```bash
# Show all history
history

# Show last 10 commands
history 10
```

### Save and restore history

```bash
# Save current history to a file
history -w ~/my_history.txt

# Load history from a file
history -r ~/my_history.txt

# Append only new commands to file
history -a ~/my_history.txt
```

### Clear history

```bash
# Clear in-memory history (file is not affected)
history -c
```

## Configuration

History behavior can be configured through environment variables:

| Variable | Description                                                                             |
|----|-----------------------------------------------------------------------------------------|
| `JSH_HISTFILE` | Path to the default history file                                                        |
| `JSH_HISTSIZE` | Maximum number of entries kept in memory                                                |
| `JSH_HISTFILESIZE` | Maximum number of entries written to file (applied on `-w`)                             |
| `INC_APPEND_HISTORY` | When sen to `true` - commands are automatically written to history file after execution |
## Use Cases

### Syncing history between terminals

If you want commands from one terminal to be available in another:

```bash
# In terminal 1: append new commands to file
history -a

# In terminal 2: read the updated file
history -r
```

### Backing up history before clearing

```bash
history -w ~/history_backup.txt
history -c
```

### Starting fresh without losing history

```bash
# Save current history
history -w

# Clear memory
history -c

# Continue working... new commands won't mix with old ones
```

## Notes

- `history -c` clears only the in-memory history. The history file remains unchanged until you explicitly write to it with `-w`.
- `history -a` appends only commands executed since the last write/append operation. This prevents duplicates when syncing between terminals.
- When using `-w`, if the history exceeds `JSH_HISTFILESIZE`, only the most recent entries are saved.