package com.laconic.hyperxengine

interface EmulatorActivity {
    abstract fun maybeRestoreState()
    abstract fun maybeSaveState()
    abstract fun toggleConsoleState()
    abstract fun reset()
}