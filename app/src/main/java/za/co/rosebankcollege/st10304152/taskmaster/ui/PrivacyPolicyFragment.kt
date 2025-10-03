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

class PrivacyPolicyFragment : Fragment() {

    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_privacy_policy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        webView = view.findViewById(R.id.webView)
        setupWebView()
        loadPrivacyPolicy()
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

    private fun loadPrivacyPolicy() {
        val htmlContent = getPrivacyPolicyContent()
        webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun getPrivacyPolicyContent(): String {
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
            </style>
        </head>
        <body>
            <h1>Privacy Policy</h1>
            <p class="last-updated">Last updated: ${java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())}</p>
            
            <h2>1. Information We Collect</h2>
            <p>TaskMaster collects the following information to provide you with our task management services:</p>
            <ul>
                <li><strong>Account Information:</strong> Email address, display name, and profile picture</li>
                <li><strong>Task Data:</strong> Task titles, descriptions, due dates, priorities, and completion status</li>
                <li><strong>App Usage:</strong> Settings preferences, language selection, and theme preferences</li>
                <li><strong>Device Information:</strong> Device type, operating system version, and app version</li>
            </ul>

            <h2>2. How We Use Your Information</h2>
            <p>We use your information to:</p>
            <ul>
                <li>Provide and maintain our task management services</li>
                <li>Sync your data across devices</li>
                <li>Personalize your app experience</li>
                <li>Send you important updates about the app</li>
                <li>Improve our services and develop new features</li>
            </ul>

            <h2>3. Data Storage and Security</h2>
            <p>Your data is stored securely using Firebase, which provides enterprise-grade security. We implement appropriate technical and organizational measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction.</p>

            <h2>4. Data Sharing</h2>
            <p>We do not sell, trade, or otherwise transfer your personal information to third parties without your consent, except as described in this privacy policy. We may share your information only in the following circumstances:</p>
            <ul>
                <li>With your explicit consent</li>
                <li>To comply with legal obligations</li>
                <li>To protect our rights and prevent fraud</li>
            </ul>

            <h2>5. Your Rights</h2>
            <p>You have the right to:</p>
            <ul>
                <li>Access your personal data</li>
                <li>Correct inaccurate data</li>
                <li>Delete your account and data</li>
                <li>Export your data</li>
                <li>Withdraw consent at any time</li>
            </ul>

            <h2>6. Data Retention</h2>
            <p>We retain your personal information for as long as your account is active or as needed to provide you with our services. You can delete your account at any time, and we will delete your data within 30 days.</p>

            <h2>7. Children's Privacy</h2>
            <p>Our services are not intended for children under 13 years of age. We do not knowingly collect personal information from children under 13.</p>

            <h2>8. Changes to This Policy</h2>
            <p>We may update this privacy policy from time to time. We will notify you of any changes by posting the new privacy policy in the app and updating the "Last updated" date.</p>

            <div class="contact-info">
                <h3>Contact Us</h3>
                <p>If you have any questions about this privacy policy, please contact us at:</p>
                <p><strong>Email:</strong> privacy@taskmaster.app</p>
                <p><strong>Address:</strong> Rosebank College, South Africa</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
