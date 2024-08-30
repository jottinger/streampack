## URL Title Operations

This is a simple operation that, upon seeing a url in a content stream, will generate a message containing the titles associated with the URLs.

This is primarily useful for mediums like IRC, because Discord and Slack clients echo the contents of URLs automatically, generally speaking; even the use of URL shorteners will display the contents of the actual (unshortened) URL. Many IRC clients will, as well, especially graphical clients.

However, for text-based IRC clients, this can help, by displaying the title of URLs whose content doesn't *seem to* match the URL.

Thus,a url like `http://foo.com/this-is-about-bar` with a title of "This is about bar" has a high congruence, and readers will be able to tell if they're interested in reading it from the URL itself.

But if the url is shortened - `http://t.c0/a7126` - users would have to open the link to see what it's about. The URLTitleOperation will open the URL (and find its actual canonical URL, although this isn't displayed) and show the title: "This is about bar," for example, assuming the validity of the link.
