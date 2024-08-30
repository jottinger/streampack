# StreamPack

An extensible bot

Nevet is a "bot" - a program that monitors different input streams and responds on them - designed for multiple input
streams and extensible features.

The original design was based on a simple IRC bot - an infobot - that would lurk on an IRC channel (or set of channels)
watching for information that justified a response. As something inspired by infobot, that means having a set of "
factoids" - things that it knows about that can be queried - among other features.

But that feels limiting: why only IRC? So it's designed around a whiteboard paradigm: it's really an information
processor. Something - a service - posts a message - like a note - to a whiteboard, and a set of actors examines the
note to see if they can or should respond to it.

If they should, then they post their *own* note back to the whiteboard, and the system will inform every service that a
response has been posted, and they can do with that note as they like.

Therefore, one can imagine a simple interaction looking like this:

1. IRC Service gets a query from a channel user.
2. IRC Service posts the query to the whiteboard.
3. The Get Karma Operation can't respond to the query, so it ignores it.
4. The Set Karma Operation can't respond to the query, so it ignores it.
5. The Get Factoid Operation can respond to the query, so it looks up the factoid and posts it back to the whiteboard.
6. The IRC Service gets notified of the response, and posts it back to the channel.

This is a *very* simple example, with one service and only a few operations (related to karma and factoids), but you can
imagine a whole host of services - like Discord, Slack, Matrix, even email or SMS - all using the same whiteboard, and
responding in kind as the specific service needs.

The whiteboard flow has some rules and a lot of features.

First, each operation has a "can you handle this?" mechanism; if it responds in the negative, the message isn't
dispatched to the operation *at all*.

Second, each operation has a "priority" - where lower numbers indicate higher priority. This is important because a
message will *not* be dispatched to operations *once an operation has responded to a message* - every operation is
potentially terminal. In the simple example above, there might be a "Set Factoid Operation," for example, but because
the "Get Factoid Operation" responded to the message, the message won't get dispatched to the "Set Factoid Operation" if
it happens to be *after* the "Get Factoid Operation."

Third, if an operation responds to a message, there's a set of transformation operations that can be applied. For
example, if a factoid contains profanity, a profanity filter might trim out the profanity... but *only* if an operation
responds. Filters aren't applied to messages that are sourced from services, only from operations. As with operations,
the filter is queried to see if it can process the message before dispatch occurs.

If *no* operation responds to the message posted to the whiteboard, the message is then posted verbatim to *all*
services *except* the originating service... assuming they can handle the message.

## Current Status

It connects to IRC; factoid support is in place, as is karma, a calculator, RSS support, and a facility by which URL
titles are converted. There's even an LLM application that connects to a local
ollama3 instance for some funny (not really useful) features that demonstrate something similar to langchain, where you
can trigger an AI operation and pass it through multiple AI phases.

Discord support might be enabled via https://github.com/zachbr/Dis4IRC/tree/master or something like it.
