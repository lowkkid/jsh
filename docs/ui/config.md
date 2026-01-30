# UI Configuration

This document describes how to customize the appearance of the JSH prompt.

## Config File Location

By default, JSH reads the UI configuration from:

```
~/.jshui
```

You can override this path by setting the `JSH_UI_CONFIG` system property:

```bash
export JSH_UI_CONFIG=/path/to/config.json
```

## File Format

The configuration file uses JSON format.

If the config file doesn't exist, JSH uses default values and works normally.

## Font Requirements

For powerline-style separators (triangle, rounded) to display correctly, you need a font with Powerline/Nerd Font symbols installed. Recommended fonts:

- [Nerd Fonts](https://www.nerdfonts.com/) (e.g., FiraCode Nerd Font, JetBrainsMono Nerd Font)
- [Powerline Fonts](https://github.com/powerline/fonts)

If you don't have these fonts, use `"FLAT"` separators or no separators at all.

## Configuration Options

### Full Example

```json
{
  "includeUser": true,
  "userStyle": {
    "foreground": "black",
    "background": "green",
    "bold": true,
    "italic": false
  },
  "userSeparator": "TRIANGLE",
  "pathStyle": {
    "foreground": "white",
    "background": "blue",
    "bold": true,
    "italic": false
  },
  "pathSeparator": "TRIANGLE",
  "promptSymbol": "$",
  "promptSymbolStyle": {
    "foreground": "white",
    "background": null,
    "bold": false,
    "italic": false
  }
}
```

### Options Reference

| Option | Type | Default | Description |
|--------|------|---------|-------------|
| `includeUser` | boolean | `false` | Show username segment |
| `userStyle` | StyleConfig | — | Style for username segment |
| `userSeparator` | string | `"TRIANGLE"` | Separator after username |
| `pathStyle` | StyleConfig | white on blue, bold | Style for path segment |
| `pathSeparator` | string | `"TRIANGLE"` | Separator after path |
| `promptSymbol` | string | `null` | Prompt symbol (e.g., `$`, `>`) |
| `promptSymbolStyle` | StyleConfig | — | Style for prompt symbol |

### StyleConfig

Each style config has these properties:

| Property | Type | Default | Description |
|----------|------|---------|-------------|
| `foreground` | string | `null` | Text color |
| `background` | string | `null` | Background color |
| `bold` | boolean | `false` | Bold text |
| `italic` | boolean | `false` | Italic text |

### Available Colors

- `black`
- `red`
- `green`
- `yellow`
- `blue`
- `magenta`
- `cyan`
- `white`
- `bright`

Use `null` for transparent background or terminal default foreground.

### Separator Styles

| Value | Symbol | Description |
|-------|--------|-------------|
| `FLAT` | (none) | No separator, flat edge |
| `TRIANGLE` | `` | Triangle/arrow separator |
| `ROUNDED` | `` | Rounded separator |

Separators automatically use the correct colors for smooth transitions between segments (powerline style).

## Minimal Examples

### Path only (default-like)

```json
{
  "pathStyle": {
    "foreground": "white",
    "background": "blue",
    "bold": true
  },
  "pathSeparator": "TRIANGLE"
}
```

### User + Path

```json
{
  "includeUser": true,
  "userStyle": {
    "foreground": "black",
    "background": "green",
    "bold": true
  },
  "pathStyle": {
    "foreground": "white",
    "background": "blue",
    "bold": true
  }
}
```

### With prompt symbol

```json
{
  "pathStyle": {
    "foreground": "white",
    "background": "blue"
  },
  "pathSeparator": "TRIANGLE",
  "promptSymbol": "$",
  "promptSymbolStyle": {
    "foreground": "green",
    "bold": true
  }
}
```

### No separators (flat style)

```json
{
  "includeUser": true,
  "userStyle": {
    "foreground": "black",
    "background": "green"
  },
  "userSeparator": "FLAT",
  "pathStyle": {
    "foreground": "white",
    "background": "blue"
  },
  "pathSeparator": "FLAT"
}
```
