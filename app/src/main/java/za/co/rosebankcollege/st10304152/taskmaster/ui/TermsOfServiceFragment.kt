package za.co.rosebankcollege.st10304152.taskmaster.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import za.co.rosebankcollege.st10304152.taskmaster.R

class TermsOfServiceFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_terms_of_service, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        setupWebView()
        loadTermsOfService()
        setupToolbar()
    }

    private fun setupToolbar() {
        view?.findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)?.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                return false
            }
        }
    }

    private fun loadTermsOfService() {
        val htmlContent = getTermsOfServiceContent()
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun getTermsOfServiceContent(): String {
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @media (prefers-color-scheme: dark) {
                    body {
                        background-color: #121212;
                        color: #ffffff;
                    }
                    h1 { color: #90caf9; border-bottom-color: #90caf9; }
                    h2 { color: #e0e0e0; }
                    h3 { color: #b0b0b0; }
                    .last-updated { color: #888; }
                    .contact-info { background-color: #1e1e1e; color: #ffffff; }
                    .highlight { background-color: #2d2d2d; color: #ffffff; border-left-color: #ffc107; }
                }
                @media (prefers-color-scheme: light) {
                    body {
                        background-color: #ffffff;
                        color: #333333;
                    }
                    h1 { color: #1976d2; border-bottom-color: #1976d2; }
                    h2 { color: #424242; }
                    h3 { color: #616161; }
                    .last-updated { color: #666; }
                    .contact-info { background-color: #f5f5f5; color: #333333; }
                    .highlight { background-color: #fff3cd; color: #333333; border-left-color: #ffc107; }
                }
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                    line-height: 1.6;
                    max-width: 800px;
                    margin: 0 auto;
                    padding: 20px;
                }
                h1 { border-bottom: 2px solid; padding-bottom: 10px; }
                h2 { margin-top: 30px; }
                p { margin-bottom: 15px; }
                ul { margin-bottom: 15px; }
                li { margin-bottom: 5px; }
                .last-updated { font-style: italic; }
                .contact-info { padding: 15px; border-radius: 8px; margin-top: 20px; }
                .highlight { padding: 10px; border-radius: 5px; border-left: 4px solid #ffc107; }
            </style>
        </head>
        <body>
            <h1>Terms of Service</h1>
            <p class="last-updated">Last updated: ${java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}</p>
            
            <div class="highlight">
                <p><strong>Important:</strong> By using TaskMaster, you agree to be bound by these Terms of Service. Please read them carefully.</p>
            </div>

            <h2>1. Acceptance of Terms</h2>
            <p>By downloading, installing, or using the TaskMaster mobile application ("App"), you agree to be bound by these Terms of Service ("Terms"). If you do not agree to these Terms, please do not use our App.</p>

            <h2>2. Description of Service</h2>
            <p>TaskMaster is a task management application that allows users to:</p>
            <ul>
                <li>Create, edit, and organize tasks</li>
                <li>Set due dates and priorities</li>
                <li>Track task completion status</li>
                <li>Sync data across multiple devices</li>
                <li>Customize app settings and preferences</li>
            </ul>

            <h2>3. User Accounts</h2>
            <p>To use TaskMaster, you must create an account by providing accurate and complete information. You are responsible for:</p>
            <ul>
                <li>Maintaining the confidentiality of your account credentials</li>
                <li>All activities that occur under your account</li>
                <li>Notifying us immediately of any unauthorized use</li>
                <li>Ensuring your account information remains accurate and up-to-date</li>
            </ul>

            <h2>4. Acceptable Use</h2>
            <p>You agree to use TaskMaster only for lawful purposes and in accordance with these Terms. You agree NOT to:</p>
            <ul>
                <li>Use the App for any illegal or unauthorized purpose</li>
                <li>Attempt to gain unauthorized access to our systems</li>
                <li>Interfere with or disrupt the App's functionality</li>
                <li>Upload malicious code or harmful content</li>
                <li>Violate any applicable laws or regulations</li>
            </ul>

            <h2>5. Intellectual Property</h2>
            <p>The TaskMaster App, including its design, functionality, and content, is protected by intellectual property laws. You may not:</p>
            <ul>
                <li>Copy, modify, or distribute the App</li>
                <li>Reverse engineer or attempt to extract source code</li>
                <li>Use our trademarks or logos without permission</li>
                <li>Create derivative works based on our App</li>
            </ul>

            <h2>6. Data and Privacy</h2>
            <p>Your privacy is important to us. Please review our Privacy Policy to understand how we collect, use, and protect your information. By using our App, you consent to the collection and use of your data as described in our Privacy Policy.</p>

            <h2>7. Service Availability</h2>
            <p>We strive to provide reliable service, but we cannot guarantee that the App will be available at all times. We may:</p>
            <ul>
                <li>Perform maintenance and updates</li>
                <li>Experience technical difficulties</li>
                <li>Modify or discontinue features</li>
                <li>Suspend service for security reasons</li>
            </ul>

            <h2>8. Limitation of Liability</h2>
            <p>To the maximum extent permitted by law, TaskMaster and its developers shall not be liable for any indirect, incidental, special, consequential, or punitive damages, including but not limited to loss of profits, data, or use, arising from your use of the App.</p>

            <h2>9. Termination</h2>
            <p>We may terminate or suspend your account at any time, with or without notice, for any reason, including violation of these Terms. You may also terminate your account at any time by deleting the App and contacting us.</p>

            <h2>10. Changes to Terms</h2>
            <p>We reserve the right to modify these Terms at any time. We will notify users of significant changes through the App or other means. Your continued use of the App after changes constitutes acceptance of the new Terms.</p>

            <h2>11. Governing Law</h2>
            <p>These Terms are governed by the laws of South Africa. Any disputes arising from these Terms or your use of the App will be subject to the jurisdiction of South African courts.</p>

            <div class="contact-info">
                <h3>Contact Information</h3>
                <p>If you have any questions about these Terms of Service, please contact us:</p>
                <p><strong>Email:</strong> legal@taskmaster.app</p>
                <p><strong>Address:</strong> Rosebank College, South Africa</p>
                <p><strong>Phone:</strong> +27 (0) 11 123 4567</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
