// The iOS entry (A1-2; pre-build T10): a SwiftUI app whose one window hosts the Compose view controller from the Kotlin
// framework (app/src/iosMain/.../MainViewController.kt), ignoring the safe areas so the colour fills the screen.
import SwiftUI
import UIKit
import ForagerApp

@main
struct ForagerAppMain: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
    }
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
