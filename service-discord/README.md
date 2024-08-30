# Discord

Running `streampack` on discord is interesting, because the Discord API is a one-size-fits-all connector.

Getting a discord connection means being connected to multiple "discords" - servers, I guess - with one connection, which means we need to think about how events get propagated through the whiteboard.

`RouterMessage` objects have a context - in IRC, that's a channel, because the IRC service connects to one (and only one) IRC network. In Discord, though, a single service can be connected to multiple **guilds** (the Discord API term), such that each guild can have multiple channels, including with the same names (and that will be quite common).

So: if a whiteboard event comes in from IRC, with a context of `#java`, how can (or should) that be mapped to Discord? If it comes in from Discord, how should the router context be constructed such that we can route responses back to the right guild?

Perhaps propagation isn't the right concept - maybe we need to add an origin to `RouterMessage` such that the `context` can be decoded correctly (i.e., Discord responds to events whose origin is Discord, not IRC, and vice versa, so the context can be considered to be service-related and not generalized.)

The problem here is the logging, though: the `lib-irclog` is targeted for IRC, and we may want to generalize *that*, too, to include the source service, so we can use the same mechanism to log from other services without polluting the module namespace. Likewise, the `lib-whiteboard` security model has a `cloak` - targeted towards awarding admin privileges to users who're properly authorized on `libera.chat` - and we may want to extend that to discord mappings, too. 

Maybe make the cloak a multifield (like the permissions) and include discord author names? No, because a cloak might "look like" an author name. It'd need to be a different field.
