package com.burgerhouse.app.ui.screens.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burgerhouse.app.data.local.entity.Product
import com.burgerhouse.app.domain.model.ProductCategory
import com.burgerhouse.app.ui.components.EmptyState
import com.burgerhouse.app.ui.components.ProductCard
import com.burgerhouse.app.ui.components.SnackbarObserver
import com.burgerhouse.app.viewmodel.AuthViewModel
import com.burgerhouse.app.viewmodel.MenuViewModel

/** Dados do banner de promoção do dia. */
private data class Promo(val title: String, val subtitle: String, val emoji: String)

private val promos = listOf(
    Promo("Combo Duplo", "Dois hambúrgueres + frita por R$ 49,90", "🍔"),
    Promo("Frete Grátis", "Use o cupom FRETEGRATIS hoje", "🛵"),
    Promo("Fidelidade", "10 pontos = hambúrguer grátis", "🎁")
)

/**
 * Home / Cardápio: header com logo, usuário e carrinho (badge com
 * animação de pulso), campo de busca com debounce, chips de categorias,
 * carrossel de promoções e lista de produtos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (Long) -> Unit,
    onCartClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
    menuViewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory)
) {
    val user by authViewModel.user.collectAsState()
    val products by menuViewModel.products.collectAsState()
    val query by menuViewModel.query.collectAsState()
    val selectedCategory by menuViewModel.selectedCategory.collectAsState()
    val cartCount by menuViewModel.cartCount.collectAsState()
    val isLoading by menuViewModel.isLoading.collectAsState()

    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    SnackbarObserver(snackbarHostState)

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // ---------- Header ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🍔", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Burger House",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Olá, ${user?.name?.substringBefore(' ') ?: "cliente"}!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Ícone do carrinho com badge pulsante
                    IconButton(onClick = onCartClick) {
                        Box {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Carrinho",
                                tint = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.size(26.dp)
                            )
                            if (cartCount > 0) {
                                val pulseTransition = rememberInfiniteTransition(label = "badge")
                                val badgeScale by pulseTransition.animateFloat(
                                    initialValue = 1f,
                                    targetValue = 1.2f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(600),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "badgeScale"
                                )
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .scale(badgeScale)
                                ) {
                                    Text("$cartCount")
                                }
                            }
                        }
                    }
                }
            }

            // ---------- Busca ----------
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { menuViewModel.onQueryChange(it) },
                    placeholder = { Text("Buscar no cardápio…") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { menuViewModel.clearQuery() }) {
                                Icon(Icons.Default.Close, contentDescription = "Limpar busca")
                            }
                        }
                    },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ---------- Chips de categorias ----------
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ProductCategory.entries.forEach { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = {
                                menuViewModel.selectCategory(
                                    if (selectedCategory == category) null else category
                                )
                            },
                            label = { Text(category.title) },
                            shape = MaterialTheme.shapes.small
                        )
                    }
                }
            }

            // ---------- Carrossel de promoções ----------
            item {
                val pagerState = rememberPagerState(pageCount = { promos.size })
                HorizontalPager(
                    state = pagerState,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    pageSpacing = 12.dp,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) { index ->
                    val promo = promos[index]
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(
                                text = "${promo.emoji}  ${promo.title}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = promo.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // ---------- Lista de produtos ----------
            item {
                Text(
                    text = "Cardápio (${products.size})",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (isLoading) {
                items(4) {
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                }
            } else if (products.isEmpty()) {
                item {
                    EmptyState(
                        emoji = "🔍",
                        title = "Nada encontrado",
                        message = "Tente outro termo de busca ou outra categoria."
                    )
                }
            } else {
                items(products, key = { it.id }) { product: Product ->
                    ProductCard(
                        product = product,
                        onAddClick = { menuViewModel.quickAddToCart(product) },
                        onClick = { onProductClick(product.id) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}
