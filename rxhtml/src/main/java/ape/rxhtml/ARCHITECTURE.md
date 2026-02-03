# RxHTML Module Architecture

The RxHTML module is a reactive HTML templating engine that compiles RxHTML templates into JavaScript code for client-side reactive rendering. It provides a declarative way to build dynamic web interfaces that automatically update when underlying data changes.

## Overview

RxHTML takes HTML-like templates with reactive bindings and compiles them into JavaScript that integrates with the Adama runtime. The compilation pipeline includes parsing, preprocessing, type checking, code generation, and routing table construction.

## Package Structure

```
ape.rxhtml
├── acl/             # Action Command Language for event handling
│   └── commands/    # Individual command implementations
├── atl/             # Attribute Template Language for data binding
│   └── tree/        # AST nodes for ATL expressions
├── codegen/         # JavaScript code generation utilities
├── preprocess/      # Template preprocessing transformations
│   └── expand/      # Static object expansion
├── routing/         # URL routing and path matching
├── server/          # Server-side rendering support
├── template/        # Core template processing
│   ├── config/      # Template configuration
│   └── sp/          # State path navigation instructions
└── typing/          # Type inference and checking
```

## Core Components

### Entry Points

| Class | Purpose |
|-------|---------|
| `RxHtmlTool` | Main entry point for converting RxHTML to JavaScript |
| `Bundler` | Bundles multiple RxHTML files into a single forest |
| `Loader` | Parses and preprocesses the HTML forest document |
| `RxHtmlBundle` | Result bundle containing JavaScript, CSS, shell, and routing |
| `TypeChecker` | Entry point for template type checking |

### ACL (Action Command Language)

The ACL package handles parsing and code generation for event-driven actions in templates. Commands are triggered by user interactions (clicks, form submissions, etc.).

**Key Classes:**
- `Parser` - Parses action command strings like `"submit"`, `"set:foo=bar"`, `"goto:/path"`
- `Command` - Interface for all command implementations

**Command Types:**
- Form: `Submit`, `SubmitById`, `Reset`, `Finalize`
- State: `Set`, `Toggle`, `Increment`, `Decrement`
- Navigation: `Goto`, `Scroll`, `Reload`
- Selection: `Choose`, `Decide`
- Authentication: `SignOut`, `ForceAuth`

### ATL (Attribute Template Language)

The ATL package handles parsing reactive expressions within attribute values and text content. Expressions like `{data.name}` or `{data.visible ? 'show' : 'hide'}` are parsed into an AST.

**Key Classes:**
- `Parser` - Parses ATL expressions into Tree nodes
- `TokenStream` - Tokenizes ATL syntax
- `Context` - Provides context for expression evaluation

**Tree Nodes:**
- `Lookup` - Variable reference (`{data.field}`)
- `Condition` - Conditional expression (`{#if ...}`)
- `Concat` - String concatenation
- `Transform` - Value transformation pipeline
- `Text` - Static text content

### Template Processing

The template package is the core of the compilation pipeline, converting parsed HTML elements into JavaScript code.

**Key Classes:**
- `Environment` - Immutable context carrying state through template traversal
- `Root` - Entry point for template and page code generation
- `Base` - Base element processing logic
- `Elements` - Handlers for specific RxHTML elements
- `Shell` - HTML shell/wrapper generation

**Processing Flow:**
1. Parse HTML into JSoup Document
2. Apply preprocessing transformations
3. Generate JavaScript via `Root.start()`, `Root.template()`, `Root.page()`, `Root.finish()`
4. Emit code using `Writer` with proper indentation

### Routing

The routing package builds URL routing tables from page definitions.

**Key Classes:**
- `Table` - Main routing table mapping URIs to targets
- `Path` - Trie-like structure for path matching
- `Instructions` - Parsed route pattern with parameter extraction
- `Target` - Route destination with headers and content

### Server-Side Rendering

The server package provides server-side page rendering capabilities.

**Key Classes:**
- `ServerPageShell` - Wraps content in server-side HTML shell
- `ServerSideTarget` - Server-rendered page target
- `RemoteInlineResolver` - Resolves remote content for inline rendering

### Preprocessing

The preprocess package transforms the parsed document before code generation.

**Key Classes:**
- `Pagify` - Applies common-page rules and template injection
- `Mobilify` - Handles mobile-specific transformations
- `ExpandStaticObjects` - Expands static object definitions
- `MarkStaticContent` - Identifies static vs dynamic content

### Type Checking

The typing package performs type inference on view state objects.

**Key Classes:**
- `ViewScope` - Hierarchical scope for view type inference
- `ViewSchemaBuilder` - Builds view schema from template analysis
- `RxRootEnvironment` - Root environment for type checking
- `DataScope` - Scope for data type tracking

## Compilation Pipeline

```
RxHTML Files
     │
     ▼
┌─────────┐
│ Bundler │  Combine multiple files into forest
└────┬────┘
     │
     ▼
┌─────────┐
│ Loader  │  Parse HTML + Preprocess (Mobilify, Pagify, ExpandStaticObjects)
└────┬────┘
     │
     ▼
┌─────────────┐
│ TypeChecker │  Validate and infer types
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ RxHtmlTool  │  Generate JavaScript + Build routing table
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ RxHtmlBundle│  JavaScript + CSS + Shell + Routing
└─────────────┘
```

## Key Design Patterns

1. **Immutable Environment**: Template processing uses immutable `Environment` objects with builder methods (`stateVar()`, `parentVariable()`) to track state without mutation.

2. **Visitor Pattern**: Tree structures (ATL nodes, path instructions) use visitor patterns for type writing and code generation.

3. **Command Pattern**: ACL commands implement a common interface for consistent event handling code generation.

4. **Trie-based Routing**: URL routing uses a trie structure in `Path` for efficient prefix matching.

## Dependencies

- **JSoup**: HTML parsing and manipulation
- **Jackson**: JSON processing for type schemas
- **Netty**: Query string encoding/decoding for server-side rendering
