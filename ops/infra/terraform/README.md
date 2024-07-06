# Terraform

`HCLOUD_TOKEN`: hetzner token created on a per-project level. Same with API
tokens.

## Notes


### Project Strucure

* `/environments/<env>/<id>`: considered the 'root' level, where project is
  init, and 'apply' is run.
* `/globals/`: shared provider, variables across all envs.
* `/modules`: combinations of resources to be imported (module source) in
  `/environments/`.

### Variables

ENV Variables

* For submodules, variables are passed in.
* For root (Executing level) module, can access env variables set via
  `TF_VAR_<name>`

When to declare a variable in `variables.tf`?

* any `var.*` usage in a `main.tf`, should have a `variables.tf` declaration at
  same level.
  * if no `var.`, (e.g. static value), don't need to declare it. We're not
    declaring parameter names, but the variables being used at that level. (A
    module's parameters will be declared at module level, as they would be
    `var.X`.)

* Think of modules like separate functions; values are passed from top. And at
  any level if a `var.*` is used within the module, it needs to be declared at
  the accompanying `variables.tf` level.
