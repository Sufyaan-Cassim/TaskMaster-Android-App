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
                    .highlight { background-color: #2d2d2d; color: #ffffff; border-left-color: #ffc107; }
                }
                @media (prefers-color-scheme: light) {
                    body { background-color: #ffffff; color: #333333; }
                    h1 { color: #1976d2; border-bottom-color: #1976d2; }
                    h2 { color: #424242; }
                    h3 { color: #616161; }
                    .last-updated { color: #666; }
                    .contact-info { background-color: #f5f5f5; color: #333333; }
                    .highlight { background-color: #fff3cd; color: #333333; border-left-color: #ffc107; }
                }
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; line-height: 1.6; max-width: 800px; margin: 0 auto; padding: 20px; }
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
            <h1>${getString(R.string.terms_of_service)}</h1>
            <p class="last-updated">${getString(R.string.last_updated)}: $formattedDate</p>
            
            <div class="highlight">
                <p><strong>${getString(R.string.terms_important)}:</strong> ${getString(R.string.terms_important_note)}</p>
            </div>

            <h2>${getString(R.string.terms_acceptance_title)}</h2>
            <p>${getString(R.string.terms_acceptance_desc)}</p>

            <h2>${getString(R.string.terms_description_title)}</h2>
            <p>${getString(R.string.terms_description_desc)}</p>
            <ul>
                <li>${getString(R.string.terms_service_item1)}</li>
                <li>${getString(R.string.terms_service_item2)}</li>
                <li>${getString(R.string.terms_service_item3)}</li>
                <li>${getString(R.string.terms_service_item4)}</li>
                <li>${getString(R.string.terms_service_item5)}</li>
            </ul>

            <h2>${getString(R.string.terms_accounts_title)}</h2>
            <p>${getString(R.string.terms_accounts_desc)}</p>
            <ul>
                <li>${getString(R.string.terms_accounts_item1)}</li>
                <li>${getString(R.string.terms_accounts_item2)}</li>
                <li>${getString(R.string.terms_accounts_item3)}</li>
                <li>${getString(R.string.terms_accounts_item4)}</li>
            </ul>

            <h2>${getString(R.string.terms_acceptable_title)}</h2>
            <p>${getString(R.string.terms_acceptable_desc)}</p>
            <ul>
                <li>${getString(R.string.terms_acceptable_item1)}</li>
                <li>${getString(R.string.terms_acceptable_item2)}</li>
                <li>${getString(R.string.terms_acceptable_item3)}</li>
                <li>${getString(R.string.terms_acceptable_item4)}</li>
                <li>${getString(R.string.terms_acceptable_item5)}</li>
            </ul>

            <h2>${getString(R.string.terms_ip_title)}</h2>
            <p>${getString(R.string.terms_ip_desc)}</p>
            <ul>
                <li>${getString(R.string.terms_ip_item1)}</li>
                <li>${getString(R.string.terms_ip_item2)}</li>
                <li>${getString(R.string.terms_ip_item3)}</li>
                <li>${getString(R.string.terms_ip_item4)}</li>
            </ul>

            <h2>${getString(R.string.terms_data_title)}</h2>
            <p>${getString(R.string.terms_data_desc)}</p>

            <h2>${getString(R.string.terms_availability_title)}</h2>
            <p>${getString(R.string.terms_availability_desc)}</p>
            <ul>
                <li>${getString(R.string.terms_availability_item1)}</li>
                <li>${getString(R.string.terms_availability_item2)}</li>
                <li>${getString(R.string.terms_availability_item3)}</li>
                <li>${getString(R.string.terms_availability_item4)}</li>
            </ul>

            <h2>${getString(R.string.terms_liability_title)}</h2>
            <p>${getString(R.string.terms_liability_desc)}</p>

            <h2>${getString(R.string.terms_termination_title)}</h2>
            <p>${getString(R.string.terms_termination_desc)}</p>

            <h2>${getString(R.string.terms_changes_title)}</h2>
            <p>${getString(R.string.terms_changes_desc)}</p>

            <h2>${getString(R.string.terms_governing_title)}</h2>
            <p>${getString(R.string.terms_governing_desc)}</p>

            <div class="contact-info">
                <h3>${getString(R.string.contact_information)}</h3>
                <p>${getString(R.string.terms_contact_desc)}</p>
                <p><strong>${getString(R.string.email)}:</strong> legal@taskmaster.app</p>
                <p><strong>${getString(R.string.address)}:</strong> ${getString(R.string.address_value)}</p>
                <p><strong>${getString(R.string.phone)}:</strong> ${getString(R.string.phone_value)}</p>
            </div>
        </body>
        </html>
        """.trimIndent()
    }
}
