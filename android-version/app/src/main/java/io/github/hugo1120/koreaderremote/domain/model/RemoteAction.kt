package io.github.hugo1120.koreaderremote.domain.model

enum class RemoteAction(val endpointPath: String) {
    PreviousPage("/koreader/event/GotoViewRel/-1"),
    NextPage("/koreader/event/GotoViewRel/1"),
    FullRefresh("/koreader/event/FullRefresh"),
    Suspend("/koreader/event/RequestSuspend"),
}
