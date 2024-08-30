# RSS Operations

This module supports a service that reads RSS feeds and dumps new entries to the whiteboard.

There are three operations (commands) associated with RSS feeds:

* `add`
* `delete`
* `info`

> There may be a `search` command added in the future.

Interacting with the RSS operation is done with the following structure:

```
~rss add https://enigmastation.com
~rss delete https://enigmastation.com/
~rss info https://enigmastation.com
```

URLs have to match *up to* the trailing slash; `https://enigmastation.com/` and `https://enigmastation.com` are equivalent, but `http://enigmastation.com` and `https://enigmastation.com` are not, and neither are `https://www.enigmastation.com/` and `https://enigmastation.com/`. **ONLY** the trailing slash is considered to be insignificant. (Issues and PRs would be welcomed here.) 

## Adding a feed

Adding a feed is done with `~rss add` followed by a URL that either *is* an RSS feed, or has a reference (via a `link` in the `head` of an HTML page) to an RSS feed. The RSS module *will* attempt to find an RSS feed in an HTML page if an HTML page is used. With `https://enigmastation.com`, there's a node in the body that looks like this, for example:

```html
&lt;link 
  href="https://enigmastation.com/feed/" 
  rel="alternate" 
  type="application/rss+xml" 
  title="Enigmastation.com feed" 
/&gt;
```

The URL provided in the `add` command becomes a *key* - the `delete` and `info` commands can use that key *or* the actual RSS feed url (if they're different) to query the RSS feed. Thus, to get `info` on that site, you could use `~rss info https://enigmastation.com` *or* `~rss info https://enigmastation.com/feed/`.

The addition should take place in *every context* for which the feed is relevant. For example, on IRC, if that feed has Java-related content *and* Kotlin-related content, and you wanted the updates to be posted in `#java` and `#kotlin`, you'd run the `~rss add https://enigmastation.com` in both channels.

This feed will be queried every hour for updates. If new entries are discovered, up to three new urls will be posted (selected at random, and in random order) to *each context* for which the feed is appropriate.

## Removing a feed

Removing a feed is simple: in a given context (a channel, perhaps):

```
~rss delete https://enigmastation.com
```

This feed's updates will no longer be echoed to that context. If a feed has no applicable contexts, it's removed entirely and no longer queried.

## Getting information about a feed

To get some information about a feed:

```
~rss info https://enigmastation.com
```

This command will display the feed's title, the URL, and three random entry URLs from the feed.
