# Factoids

Factoids have two primary operations: get and set.

Getting a factoid has this basic grammar:

```
~term[.readattribute]
```

A term is able to be multiple words.

Read attributes are: INFO, URLS|URL, TAGS|TAG, SEEALSO, LANGUAGES|LANGUAGE.

Setting a factoid has *this* basic grammar:

```
~term[.writeattribute][=[value]]
```

Write attributes are: INFO, URLS|URL, TAGS|TAG, SEEALSO, LANGUAGES|LANGUAGE, FORGET.

`FORGET` is special in that it has no value associated with it; it removes the factoid from the database.

If the `value` is not supplied for write attributes that have values, it removes the attribute from the factoid.
