# Factoids

Factoids have two primary operations: get and set.

## Get factoid
Getting a factoid has this basic grammar:

```
~term[.readattribute]
```

A term is able to be multiple words.

Read attributes are: `TEXT`, `URLS`|`URL`, `TAGS`|`TAG`, `SEEALSO`, `LANGUAGES`|`LANGUAGE`, `INFO`.

`INFO` describes what read attributes are set for a given term.

### Future enhancement for READ

It'd be nice to be able to set a factoid with placeholder: `{1}`, `{2}`, etc. The query then changes somewhat:

```
~term foo bar
```

For this query, if `term.text` has a value of `Hey look, {1}, it's a {2}` then this would get rendered as `Hey look, foo, it's a bar`. This requires a more complex search for `term` because the parser would first try to find `term foo bar` and, failing that, would then search for `term foo` (looking for a factoid that takes a single parameter), and then `term` (looking for a factoid that takes two parameters). The Factoid entries do have a "master record" that would have `term`, so the query would be quick (and indexed), but it's still multiple queries.

It also needs to be considered what to do if the parameter count doesn't match up: if `term` has one parameter, does that mean `bar` is discarded and `foo` is used as a replacement token? Or is this an error condition?

## Set Factoid

Setting a factoid has *this* basic grammar:

```
~term[.writeattribute][=[value]]
```

Write attributes are: `TEXT`, `URLS`|`URL`, `TAGS`|`TAG`, `SEEALSO`, `LANGUAGES`|`LANGUAGE`, `FORGET`.

`FORGET` is special in that it has no value associated with it; it removes the factoid from the database.

If the `value` is not supplied for write attributes that have values, it removes the attribute from the factoid.

### Future enhancement for WRITE

It'd be nice to be able to lock factoids cleanly. The API already supports this, but there's no command support for setting or querying locked status.
