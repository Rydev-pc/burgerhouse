package com.burgerhouse.app.ui.screens.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.ui.components.EmptyState
import com.burgerhouse.app.ui.components.ImageWithShimmer
import com.burgerhouse.app.ui.components.QuantitySelector
import com.burgerhouse.app.ui.components.SnackbarObserver
import com.burgerhouse.app.ui.util.Money
import com.burgerhouse.app.viewmodel.CartViewModel

/**
 * Carrinho: itens com quantidade editável, remoção por swipe,
 * cupom de desconto e resumo financeiro (subtotal, entrega e total).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: () -> Unit,
    cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
) {
    val cartItems by cartViewModel.cartItems.collectAsState()
    val summary by cartViewModel.summary.collectAsState()
    val couponError by cartViewModel.couponError.collectAsState()

    var couponInput by remember { mutableStateOf("") }

    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    SnackbarObserver(snackbarHostState)

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Carrinho (${summary.itemCount})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                EmptyState(
                    emoji = "🛒",
                    title = "Carrinho vazio",
                    message = "Adicione itens do cardápio para continuar."
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp, vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(cartItems, key = { it.id }) { item ->
                        // Item removível por swipe
                        val dismissState = rememberSwipeToDismissBoxState()
                        LaunchedEffect(dismissState.currentValue) {
                            if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
                                cartViewModel.removeItem(item)
                            }
                        }

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.errorContainer)
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Remover",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        ) {
                            CartItemCard(
                                item = item,
                                onIncrement = { cartViewModel.incrementQuantity(item) },
                                onDecrement = { cartViewModel.decrementQuantity(item) }
                            )
                        }
                    }

                    // Resumo financeiro
                    item {
                        SummaryCard(
                            subtotal = summary.subtotal,
                            deliveryFee = summary.deliveryFee,
                            discount = summary.discount,
                            total = summary.total,
                            couponCode = summary.couponCode,
                            couponInput = couponInput,
                            couponError = couponError,
                            onCouponInputChange = { couponInput = it },
                            onApplyCoupon = { cartViewModel.applyCoupon(couponInput) },
                            onRemoveCoupon = {
                                cartViewModel.removeCoupon()
                                couponInput = ""
                            }
                        )
                    }
                }

                // Botão finalizar pedido
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(54.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = "Finalizar pedido — ${Money.format(summary.total)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/** Card de um item do carrinho. */
@Composable
private fun CartItemCard(
    item: CartItem,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(MaterialTheme.shapes.small)
            ) {
                ImageWithShimmer(
                    url = item.imageUrl,
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 10
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Unid. ${Money.format(item.unitPrice + item.extrasPrice)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.meatPoint.isNotBlank() && item.isBurger) {
                    Text(
                        text = "Ponto: ${item.meatPoint}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                QuantitySelector(
                    quantity = item.quantity,
                    onDecrease = onDecrement,
                    onIncrease = onIncrement
                )
            }

            Text(
                text = Money.format(item.totalPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** Card de resumo financeiro + cupom de desconto. */
@Composable
private fun SummaryCard(
    subtotal: Double,
    deliveryFee: Double,
    discount: Double,
    total: Double,
    couponCode: String?,
    couponInput: String,
    couponError: String?,
    onCouponInputChange: (String) -> Unit,
    onApplyCoupon: () -> Unit,
    onRemoveCoupon: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cupom
            Text(
                text = "Cupom de desconto",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = couponInput,
                    onValueChange = onCouponInputChange,
                    placeholder = { Text("Ex: BURGER10") },
                    singleLine = true,
                    isError = couponError != null,
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(onClick = onApplyCoupon) { Text("Aplicar") }
            }
            if (couponError != null) {
                Text(
                    text = couponError,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }
            if (couponCode != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    Text(
                        text = "Cupom $couponCode aplicado ✓",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onRemoveCoupon) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remover cupom",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Valores
            SummaryRow("Subtotal", subtotal)
            SummaryRow("Taxa de entrega", deliveryFee)
            if (discount > 0) {
                SummaryRow("Desconto", -discount, highlight = true)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Total",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = Money.format(total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun SummaryRow(label: String, value: Double, highlight: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Start
        )
        Text(
            text = Money.format(value),
            style = MaterialTheme.typography.bodyLarge,
            color = if (highlight) MaterialTheme.colorScheme.primary
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
