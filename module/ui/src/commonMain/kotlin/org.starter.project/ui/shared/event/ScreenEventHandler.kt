package org.starter.project.ui.shared.event

interface ScreenEventHandler {
    operator fun invoke(event: ScreenEvent)
}
