package io.github.hugo1120.koreaderremote.ui.screen

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class RemoteLayoutSpecTest {
    @Test
    fun `uses compact profile at and below 700dp`() {
        val layout = remoteLayoutSpecForHeight(700)

        assertThat(layout.heightBand).isEqualTo(RemoteHeightBand.Compact)
        assertThat(layout.mainButtonHeightDp).isEqualTo(84)
        assertThat(layout.toolButtonHeightDp).isEqualTo(68)
    }

    @Test
    fun `uses regular profile between 701dp and 840dp`() {
        val layout = remoteLayoutSpecForHeight(780)

        assertThat(layout.heightBand).isEqualTo(RemoteHeightBand.Regular)
        assertThat(layout.mainButtonHeightDp).isEqualTo(92)
        assertThat(layout.toolButtonHeightDp).isEqualTo(76)
    }

    @Test
    fun `uses tall profile above 840dp`() {
        val layout = remoteLayoutSpecForHeight(900)

        assertThat(layout.heightBand).isEqualTo(RemoteHeightBand.Tall)
        assertThat(layout.mainButtonHeightDp).isEqualTo(96)
        assertThat(layout.toolButtonHeightDp).isEqualTo(80)
    }
}
