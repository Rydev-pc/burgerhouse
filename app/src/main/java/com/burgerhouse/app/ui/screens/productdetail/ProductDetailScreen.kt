package com.burgerhouse.app.ui.screens.productdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burgerhouse.app.data.local.entity.CartItem
import com.burgerhouse.app.domain.model.Extra
import com.burgerhouse.app.domain.model.MeatPoint
import com.burgerhouse.app.ui.components.ImageWithShimmer
import com.burgerhouse.app.ui.components.QuantitySelector
import com.burgerhouse.app.ui.components.SnackbarObserver
import com.burgerhouse.app.ui.util.Money
import com.burgerhouse.app.viewmodel.CartViewModel
import com.burgerhouse.app.viewmodel.MenuViewModel

/**
 * Detalhe do produto: imagem grande, seleção de adicionais, ponto da
 * carne, quantidade, observações e preço total em tempo real.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProductDetailScreen(
    productId: Long,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    menuViewModel: MenuViewModel = viewModel(factory = MenuViewModel.Factory),
    cartViewModel: CartViewModel = viewModel(factory = CartViewModel.Factory)
) {
    val product by menuViewModel.selectedProduct.collectAsState()

    // Estados das opções
    var selectedExtras by remember { mutableStateOf(setOf<Extra>()) }
    var meatPoint by remember { mutableStateOf(MeatPoint.default) }
    var quantity by remember { mutableStateOf(1) }
    var notes by remember { mutableStateOf("") }

    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    SnackbarObserver(snackbarHostState)

    // Carrega o produto ao abrir a tela
    LaunchedEffect(productId) { menuViewModel.loadProduct(productId) }

    // Preço total em tempo real
    val extrasPrice = selectedExtras.sumOf { it.price }
    val unitTotal = (product?.price ?: 0.0) + extrasPrice
    val totalPrice = unitTotal * quantity

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(product?.name ?: "Carregando…") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Imagem grande
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                ImageWithShimmer(
                    url = product?.imageUrl ?: "",
                    modifier = Modifier.fillMaxSize(),
                    cornerRadius = 0
                )
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = product?.name ?: "",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = product?.description ?: "",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Money.format(product?.price ?: 0.0),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                // ---------- Adicionais ----------
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Adicionais",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Extra.entries.forEach { extra ->
                        val selected = selectedExtras.contains(extra)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                selectedExtras = if (selected) {
                                    selectedExtras - extra
                                } else {
                                    selectedExtras + extra
                                }
                            },
                            label = { Text("${extra.label} +${Money.format(extra.price)}") },
                            leadingIcon = {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.height(16.dp)
                                    )
                                }
                            }
                        )
                    }
                }

                // ---------- Ponto da carne (apenas hambúrgueres) ----------
                if (product?.category == "BURGERS") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Ponto da carne",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        MeatPoint.entries.forEach { point ->
                            FilterChip(
                                selected = meatPoint == point,
                                onClick = { meatPoint = point },
                                label = { Text(point.label) }
                            )
                        }
                    }
                }

                // ---------- Quantidade ----------
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Quantidade",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                QuantitySelector(
                    quantity = quantity,
                    onDecrease = { if (quantity > 1) quantity-- },
                    onIncrease = { quantity++ },
                    onRemoveAtOne = false
                )

                // ---------- Observações ----------
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Observações") },
                    placeholder = { Text("Ex: sem cebola, maionese à parte…") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Botão fixo com total
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(16.dp)
            ) {
                Button(
                    onClick = {
                        val currentProduct = product ?: return@Button
                        cartViewModel.addToCart(
                            CartItem(
                                productId = currentProduct.id,
                                name = currentProduct.name,
                                imageUrl = currentProduct.imageUrl,
                                unitPrice = currentProduct.price,
                                quantity = quantity,
                                isBurger = currentProduct.category == "BURGERS",
                                bacon = selectedExtras.contains(Extra.BACON),
                                extraCheese = selectedExtras.contains(Extra.EXTRA_CHEESE),
                                egg = selectedExtras.contains(Extra.EGG),
                                caramelizedOnion = selectedExtras.contains(Extra.CARAMELIZED_ONION),
                                meatPoint = meatPoint.label,
                                notes = notes.trim()
                            )
                        )
                        onCartClick()
                    },
                    enabled = product != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.height(0.dp))
                    Text(
                        text = "  Adicionar ao carrinho — ${Money.format(totalPrice)}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
