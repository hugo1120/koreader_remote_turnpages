package io.github.hugo1120.koreaderremote.ui.screen

enum class RemoteHeightBand {
    Compact,
    Regular,
    Tall,
}

data class RemoteLayoutSpec(
    val heightBand: RemoteHeightBand,
    val horizontalPaddingDp: Int,
    val verticalPaddingDp: Int,
    val blockSpacingDp: Int,
    val maxContentWidthDp: Int,
    val headerHeightDp: Int,
    val headerPaddingHorizontalDp: Int,
    val headerPaddingVerticalDp: Int,
    val mainButtonHeightDp: Int,
    val toolButtonHeightDp: Int,
    val bottomButtonHeightDp: Int,
    val mainButtonIconSizeDp: Int,
    val toolIconSizeDp: Int,
    val chipHorizontalPaddingDp: Int,
    val chipVerticalPaddingDp: Int,
    val headerStatusMaxLines: Int,
)

fun remoteLayoutSpecForHeight(heightDp: Int): RemoteLayoutSpec =
    when {
        heightDp <= 700 -> RemoteLayoutSpec(
            heightBand = RemoteHeightBand.Compact,
            horizontalPaddingDp = 12,
            verticalPaddingDp = 12,
            blockSpacingDp = 10,
            maxContentWidthDp = 400,
            headerHeightDp = 106,
            headerPaddingHorizontalDp = 14,
            headerPaddingVerticalDp = 12,
            mainButtonHeightDp = 84,
            toolButtonHeightDp = 68,
            bottomButtonHeightDp = 42,
            mainButtonIconSizeDp = 22,
            toolIconSizeDp = 18,
            chipHorizontalPaddingDp = 8,
            chipVerticalPaddingDp = 4,
            headerStatusMaxLines = 1,
        )

        heightDp <= 840 -> RemoteLayoutSpec(
            heightBand = RemoteHeightBand.Regular,
            horizontalPaddingDp = 16,
            verticalPaddingDp = 16,
            blockSpacingDp = 12,
            maxContentWidthDp = 420,
            headerHeightDp = 118,
            headerPaddingHorizontalDp = 16,
            headerPaddingVerticalDp = 14,
            mainButtonHeightDp = 92,
            toolButtonHeightDp = 76,
            bottomButtonHeightDp = 46,
            mainButtonIconSizeDp = 24,
            toolIconSizeDp = 20,
            chipHorizontalPaddingDp = 9,
            chipVerticalPaddingDp = 5,
            headerStatusMaxLines = 2,
        )

        else -> RemoteLayoutSpec(
            heightBand = RemoteHeightBand.Tall,
            horizontalPaddingDp = 18,
            verticalPaddingDp = 20,
            blockSpacingDp = 14,
            maxContentWidthDp = 440,
            headerHeightDp = 124,
            headerPaddingHorizontalDp = 18,
            headerPaddingVerticalDp = 16,
            mainButtonHeightDp = 96,
            toolButtonHeightDp = 80,
            bottomButtonHeightDp = 48,
            mainButtonIconSizeDp = 24,
            toolIconSizeDp = 20,
            chipHorizontalPaddingDp = 10,
            chipVerticalPaddingDp = 5,
            headerStatusMaxLines = 2,
        )
    }
