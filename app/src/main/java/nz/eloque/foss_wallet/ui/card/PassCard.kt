@Composable
fun ShortPassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
) {
    val cardColors = if (pass.colors == null) {
        if (isSystemInDarkTheme()) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            CardDefaults.elevatedCardColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        }
    } else {
        pass.colors.toCardColors()
    }
    
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        ElevatedCard(
            colors = cardColors,
            modifier = modifier
                .fillMaxWidth()
                .scale(scale),
            onClick = onClick,
        ) {
            ShortPassContent(pass, cardColors)
        }

        if (selected) {
            SelectionIndicator(Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
fun PassCard(
    pass: Pass,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    selected: Boolean = false,
    colors: CardColors = CardDefaults.elevatedCardColors(),
    content: @Composable ((cardColors: CardColors) -> Unit),
) {
    val cardColors = if (pass.colors == null) {
        if (isSystemInDarkTheme()) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        } else {
            CardDefaults.elevatedCardColors(
                containerColor = Color.White,
                contentColor = Color.Black
            )
        }
    } else {
        pass.colors.toCardColors()
    }
    
    val scale by animateFloatAsState(if (selected) 0.95f else 1f)
    ElevatedCard(
        colors = cardColors,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        PassContent(pass, cardColors, Modifier, content)
    }
}
