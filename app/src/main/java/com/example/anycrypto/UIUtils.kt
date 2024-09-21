package com.tnd.anycrypto

import android.nfc.NfcAdapter
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
class UIUtils {
    companion object {
        fun updateBackgroundBasedOnNFCState(backgroundImageView: ImageView) {
            val context = backgroundImageView.context
            val nfcAdapter: NfcAdapter? = NfcAdapter.getDefaultAdapter(context)
            if (nfcAdapter != null && nfcAdapter.isEnabled) {
                // NFC is enabled - Set the image for NFC on state
                backgroundImageView.setImageResource(R.drawable.nfc_on)
            } else {
                // NFC is disabled or not available - Set the image for NFC off state
                backgroundImageView.setImageResource(R.drawable.nfc_off)
            }
        }

        fun updateCardUI(activity: AppCompatActivity, address: String, connectedNetwork: String) {
            val cardLayout = activity.findViewById<View>(R.id.cardLayout)
            val cardBackground = cardLayout.findViewById<LinearLayout>(R.id.card_background)
            val chainLogoImageView = cardLayout.findViewById<ImageView>(R.id.chainLogoImageView)
            val metamaskImageView = cardLayout.findViewById<ImageView>(R.id.metamaskImageView)
            val addressCard = cardLayout.findViewById<TextView>(R.id.addressTextView)

            when (connectedNetwork) {
                "0x89" -> {
                    cardBackground.setBackgroundResource(R.drawable.card_background_polygon)
                    chainLogoImageView.setImageResource(R.drawable.polygon)
                    metamaskImageView.visibility = View.VISIBLE
                    addressCard.text = Utils.shortenAddress(address)
                }
                "0x1" -> {
                    cardBackground.setBackgroundResource(R.drawable.card_background_eth)
                    chainLogoImageView.setImageResource(R.drawable.eth)
                    metamaskImageView.visibility = View.VISIBLE
                    addressCard.text = Utils.shortenAddress(address)
                }
            }
        }

    }
}