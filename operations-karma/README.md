# Karma Module

This is a module that targets a fun - well, *usually* fun - feature from IRC. If something happened to garner approval,
users would "increment its karma." A "high karma" indicates approval, and a "low karma" indicates *disapproval*, much as
in various Indian religions, where doing a good deed gave you "good karma" and doing something evil would give you "negative
karma."

This module allows you to use `++` to *increment* karma, and `--` to *decrement karma*, and query a thing's karmic
state.

The syntax is simple.

## Changing Karma

To change a thing's karma, you give the thing's name (or description) in text, and the karmic operation, with an
*optional* signal character, `~`, which is stripped and ignored. If you wish, you can add text after the operation, to
say *why* you're changing its karma.

Examples:

```
streampack++ good bot!
```

This will result in the bot (`streampack`, here, because you can change the bot's name if you like!) in saying
`streampack now has karma of 1.`, assuming that it had no karma beforehand.

You can also use completion syntax, from many IRC clients, where hitting `TAB` will autocomplete a nick and append a
colon and a space afterward:

```
streampack: ++ good bot!
```

Negating karma uses the same syntax, but with `--`:

```
~streampack -- I didn't meant to give you so much karma!
```

Note the signal character here, which is *purely* performative, and has no impact on the operation at all.

### Specifying karmic subjects

Karma objects can have arbitrary names: it's legitimate to run the following, for example.

```
having to use gradle to build Android-- gosh, gradle's no fun in the hands of the undisciplined
```

This will change the karma of `having to use gradle to build Android`, which *works*, but apparently is considered to
be "no fun."

The thing's name is calculated greedily to the *last operation*, so using a command like `C++` increments `C`'s karma,
while using `C++++` increments `C++`'s karma, and using `C++--` *decrements* `C++`'s karma. You can, of course, create
some weird situations with this: `C++++++` changes the karma of `C++++`.

> It's also considered *bad karma* to try to give yourself *good karma*. Leave praise to others.

## Querying Karma

The karma service also allows you to *query* karma. Query is done with a simple keyword: `karma`, followed by the thing
you wish to query. As with the operations, a signal character (`~`) is *optional*. However, completion characters are
*not* stripped.

Examples:

```
~karma streampack
karma streampack
```

## How Karma Is Calculated

Karma is calculated as an integer, as a sum of the karma others have given you. There are, however, rules.

In simple terms, if `subject++` is executed five times, the karma of `subject` will be `5`. If `subject--` is then
executed, its karma will be `4`. That's simple enough.

However, karma *erodes*. Karma points become less effective over time; karma given today is "full value" for a while,
but in a year, it will have expired completely - and just before the year expires, it still is calculated as part of the
subject's karma, but at a fraction of a point. If one karma point is awarded to a subject every week for 40 weeks, the
subject's karma will be `30`, not `40`, because of the weaker power of the "old karma points" Over a year's time - at
one point a week - the karma would be 44, not 52.

After a year's time, the karma points expire altogether and are ignored (and deleted from the datastore).

This models the possibility of passive redemption. Imagine someone who commits a horrible faux pas, and receives
sufficient scorn from their community, leading to massive negative karma. Ideally, they'd work to offset their negative
karma (by doing good deeds and receiving positive karma as a result), but it's not easy to redeem yourself in that way -
sometimes redemption is as simple as "no longer doing the wrong thing," which is a very small good deed in and of
itself, but is a good deed nonetheless. Time heals all wounds, as the saying goes, and karma expiration models the
fading of memory.
