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
        val dateFormat = java.text.SimpleDateFormat("MMMM dd, yyyy", resources.configuration.locales[0])
        val formattedDate = dateFormat.format(java.util.Date())
        
        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @media (prefers-color-scheme: dark) {
                    body { background-color: #121212; color: #ffffff; }
                    h1 { color: #90caf9; border-bottom-color: #90caf9; }
                    h2 { color: #e0e0e0; }
                    h3 { color: #b0b0b0; }
                    .last-updated { color: #888; }
                    .contact-info { background-color: #1e1e1e; color: #ffffff; }
                }
                @media (prefers-color-scheme: light) {
                    body { background-color: #ffffff; color: #333333; }
                    h1 { color: #1976d2; border-bottom-color: #1976d2; }
                    h2 { color: #424242; }
                    h3 { color: #616161; }
                    .last-updated { color: #666; }
                    .contact-info { background-color: #f5f5f5; color: #333333; }
                }
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; }
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
            <h1>${getString(R.string.privacy_policy)}</h1>
            <p class="last-updated">${getString(R.string.last_updated)}: $formattedDate</p>
            
            <h2>${getString(R.string.privacy_info_collect_title)}</h2>
            <p>${getString(R.string.privacy_info_collect_desc)}</p>
            <ul>
                <li><strong>${getString(R.string.privacy_account_info)}:</strong> ${getString(R.string.privacy_account_info_desc)}</li>
                <li><strong>${getString(R.string.privacy_task_data)}:</strong> ${getString(R.string.privacy_task_data_desc)}</li>
                <li><strong>${getString(R.string.privacy_app_usage)}:</strong> ${getString(R.string.privacy_app_usage_desc)}</li>
                <li><strong>${getString(R.string.privacy_device_info)}:</strong> ${getString(R.string.privacy_device_info_desc)}</li>
            </ul>

            <h2>${getString(R.string.privacy_how_use_title)}</h2>
            <p>${getString(R.string.privacy_how_use_desc)}</p>
            <ul>
                <li>${getString(R.string.privacy_use_item1)}</li>
                <li>${getString(R.string.privacy_use_item2)}</li>
                <li>${getString(R.string.privacy_use_item3)}</li>
                <li>${getString(R.string.privacy_use_item4)}</li>
                <li>${getString(R.string.privacy_use_item5)}</li>
            </ul>

            <h2>${getString(R.string.privacy_storage_title)}</h2>
            <p>${getString(R.string.privacy_storage_desc)}</p>

            <h2>${getString(R.string.privacy_sharing_title)}</h2>
            <p>${getString(R.string.privacy_sharing_desc)}</p>
            <ul>
                <li>${getString(R.string.privacy_sharing_item1)}</li>
                <li>${getString(R.string.privacy_sharing_item2)}</li>
                <li>${getString(R.string.privacy_sharing_item3)}</li>
            </ul>

            <h2>${getString(R.string.privacy_rights_title)}</h2>
            <p>${getString(R.string.privacy_rights_desc)}</p>
            <ul>
                <li>${getString(R.string.privacy_rights_item1)}</li>
                <li>${getString(R.string.privacy_rights_item2)}</li>
                <li>${getString(R.string.privacy_rights_item3)}</li>
                <li>${getString(R.string.privacy_rights_item4)}</li>
                <li>${getString(R.string.privacy_rights_item5)}</li>
            </ul>

            <h2>${getString(R.string.privacy_retention_title)}</h2>
            <p>${getString(R.string.privacy_retention_desc)}</p>

            <h2>${getString(R.string.privacy_children_title)}</h2>
            <p>${getString(R.string.privacy_children_desc)}</p>

            <h2>${getString(R.string.privacy_changes_title)}</h2>
            <p>${getString(R.string.privacy_changes_desc)}</p>

            <div class="contact-info">
                <h3>${getString(R.string.contact_us)}</h3>
                <p>${getString(R.string.privacy_contact_desc)}</p>
                <p><strong>${getString(R.string.email)}:</strong> privacy@taskmaster.app</p>
                <p><strong>${getString(R.string.address)}:</strong> ${getString(R.string.address_value)}</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
