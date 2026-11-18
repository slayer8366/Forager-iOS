package com.zynergylabs.forager.app

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/** The iOS entry: the Xcode side (A1-2) hosts this view controller. */
fun MainViewController(): UIViewController = ComposeUIViewController { App() }
