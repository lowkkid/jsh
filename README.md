# JSH

A modern shell written in Java.

[![CI](https://github.com/lowkkid/jsh/actions/workflows/ci.yml/badge.svg)](https://github.com/lowkkid/jsh/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=lowkkid_jsh&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=lowkkid_jsh)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

![demo](assets/demo.gif)

## About

JSH started as a [Build Your Own Shell](https://app.codecrafters.io/courses/shell/overview) challenge on [codecrafters.io](https://codecrafters.io/). Over time it grew well beyond the scope of that challenge into a standalone, more production-ready shell with features like pipelines, aliases, variables, a customizable prompt, and native binary support via GraalVM.

## Features

- 13 built-in commands (cd, echo, history, alias, export, set, dc, and more)
- Pipelines (`|`) with smart segmented execution mixing built-in and external commands
- Output redirection (`>`, `>>`, `2>`, `2>>`)
- Aliases and shell variables with export support
- RC file (`~/.jshrc`) for startup configuration
- Customizable Powerline-style prompt with colors, bold, italic, and separators
- Tab completion for commands and file paths
- Syntax highlighting for built-in commands
- Persistent command history
- Docker container TUI (`dc` command)
- GraalVM native image support for instant startup

## Installation

### Pre-built binaries (recommended)

Download the latest binary for your OS from [Releases](https://github.com/lowkkid/jsh/releases):

- `jsh-linux-amd64`
- `jsh-macos-amd64`
- `jsh-windows-amd64.exe`

```bash
chmod +x jsh-linux-amd64
./jsh-linux-amd64
```

### Build native binary

Requires GraalVM JDK 25+.

```bash
git clone https://github.com/lowkkid/jsh.git
cd jsh
mvn -Pnative compile
```

The binary will be produced in the `target/` directory.

### Development

For local development and testing only — not recommended for daily use.

```bash
./jsh.sh
```

This builds a fat JAR and runs it. Requires JDK 25+ and Maven.

## Commands

| Command   | Description                          | Docs                                  |
|-----------|--------------------------------------|---------------------------------------|
| `echo`    | Print arguments to stdout            |                                       |
| `cd`      | Change directory                     |                                       |
| `pwd`     | Print working directory              |                                       |
| `exit`    | Exit the shell                       |                                       |
| `type`    | Show if a command is built-in or external |                                  |
| `history` | View and manage command history      | [docs](docs/commands/history.md)      |
| `alias`   | Create command shortcuts             | [docs](docs/commands/aliases.md)      |
| `unalias` | Remove aliases                       |                                       |
| `set`     | Set shell variables                  | [docs](docs/commands/variables.md)    |
| `export`  | Export variables to child processes  | [docs](docs/commands/variables.md)    |
| `unset`   | Remove variables                     | [docs](docs/commands/variables.md)    |
| `dc`      | Docker container management TUI      |                                       |

## Configuration

### Prompt (`~/.jshui`)

The prompt is configured via a JSON file. Each segment (username, path, symbol) can be styled with foreground/background colors, bold, and italic. Supports Powerline-style separators (flat, triangle, rounded).

See [Prompt Configuration](docs/ui/config.md) for details and examples.

### RC file (`~/.jshrc`)

Executed on startup. Use it to define aliases, set variables, and configure your environment:

```bash
alias ll='ls -la'
alias gs='git status'
export EDITOR=vim
set greeting=hello
```

### Environment variables

| Variable            | Description                           | Default           |
|---------------------|---------------------------------------|--------------------|
| `JSH_UI_CONFIG`     | Path to UI config file                | `~/.jshui`         |
| `JSH_RC_FILE`       | Path to RC file (can't be changed)     | `~/.jshrc`         |
| `JSH_HISTFILE`      | Path to history file                  | `~/.jsh_history`   |
| `JSH_HISTSIZE`      | Max in-memory history entries         | `1000`             |
| `JSH_HISTFILESIZE`  | Max file history entries              | `2000`             |
| `INC_APPEND_HISTORY` | Auto-append history after each command | `true`         |
| `JSH_LOGS`          | Enable logging                        | `false`            |

## Architecture & Design Decisions

```
Main (REPL loop)
  → InputParser (tokenize, expand aliases & variables, detect pipes/redirects)
    → CommandRegistry (lookup built-in or external command, LRU cache)
      → SegmentedExecutor (execute single command or pipeline)
```

### Segmented pipeline execution

Pipelines are split at built-in command boundaries into segments. Consecutive external commands are grouped and executed via `ProcessBuilder.startPipeline()` for OS-level piping. Built-in commands run in the JVM with buffered I/O between segments. This means a pipeline like `cat file | grep foo | pwd | wc -l` is split into three segments — external `[cat, grep]`, built-in `[pwd]`, and external `[wc]` — each executed in the most efficient way.

### GraalVM over JVM

JSH compiles to a native binary via GraalVM. This eliminates JVM startup time and dramatically reduces memory usage (from 100MB to ~25MB), which matters for a tool you launch constantly. CI builds native binaries for Linux, macOS, and Windows on every release.

### GSON over Jackson

GSON adds ~300KB to the JAR. Jackson would add ~2MB. For a shell that only parses small config files, the lighter library wins.

### LRU command cache

external commands are cached in a 100-entry LRU map (`LinkedHashMap` with access-order). Built-in commands are protected from eviction.
### CI pipeline

[checkstyle](https://checkstyle.sourceforge.io/) gate → build & test with JaCoCo coverage → SonarCloud quality analysis.

## License

[MIT](LICENSE)
