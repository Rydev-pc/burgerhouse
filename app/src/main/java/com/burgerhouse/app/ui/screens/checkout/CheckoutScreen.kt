package com.burgerhouse.app.ui.screens.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.burgerhouse.app.data.local.entity.PaymentMethods
import com.burgerhouse.app.ui.components.SnackbarObserver
import com.burgerhouse.app.ui.util.Money
import com.burgerhouse.app.viewmodel.CheckoutViewModel

/**
 * Checkout: endereço de entrega, forma de pagamento (Dinheiro/Cartão/Pix),
 * campo de troco para pagamentos em dinheiro e resumo do pedido.
 */
@Composable
fun CheckoutScreen(
    onBack: () -> Unit,
    onOrderPlaced: (Long) -> Unit,
    checkoutViewModel: CheckoutViewModel = viewModel(factory = CheckoutViewModel.Factory)
) {
    val address by checkoutViewModel.address.collectAsState()
    val paymentMethod by checkoutViewModel.paymentMethod.collectAsState()
    val changeFor by checkoutViewModel.changeFor.collectAsState()
    val summary by checkoutViewModel.summary.collectAsState()
    val cartItems by checkoutViewModel.cartItems.collectAsState()
    val orderCreatedId by checkoutViewModel.orderCreatedId.collectAsState()
    val isSubmitting by checkoutViewModel.isSubmitting.collectAsState()

    val snackbarHostState = androidx.compose.material3.SnackbarHostState()
    SnackbarObserver(snackbarHostState)

    // Navega para o acompanhamento quando o pedido é criado
    LaunchedEffect(orderCreatedId) {
        orderCreatedId?.let { id ->
            checkoutViewModel.consumeOrderCreated()
            onOrderPlaced(id)
        }
    }

    val paymentOptions = listOf(PaymentMethods.CASH, PaymentMethods.CREDIT_CARD, PaymentMethods.PIX)

    Scaffold(
        snackbarHost = { androidx.compose.material3.SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Finalizar pedido") },
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
                .padding(16.dp)
        ) {
            // ---------- Endereço ----------
            Text(
                text = "Endereço de entrega",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = address,
                onValueChange = { checkoutViewModel.onAddressChange(it) },
                label = { Text("Endereço completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Pagamento ----------
            Text(
                text = "Forma de pagamento",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(Modifier.selectableGroup()) {
                paymentOptions.forEach { option ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = paymentMethod == option,
                            onClick = { checkoutViewModel.selectPaymentMethod(option) }
                        )
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Campo de troco (pagamento em dinheiro)
            if (paymentMethod == PaymentMethods.CASH) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = changeFor,
                    onValueChange = { checkoutViewModel.onChangeFor(it) },
                    label = { Text("Troco para (opcional)") },
                    placeholder = { Text("Ex: 50,00") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ---------- Resumo do pedido ----------
            Text(
                text = "Resumo do pedido",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            cartItems.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${item.quantity}x ${item.name}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = Money.format(item.totalPrice),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            SummaryLine("Subtotal", summary.subtotal)
            SummaryLine("Taxa de entrega", summary.deliveryFee)
            summary.couponCode?.let {
                SummaryLine("Desconto (${summary.couponCode})", -summary.discount)
            }
            Spacer(modifier = Modifier.height(6.dp))
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
                    text = Money.format(summary.total),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { checkoutViewModel.confirmOrder() },
                enabled = !isSubmitting && cartItems.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Confirmar pedido", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SummaryLine(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = Money.format(value),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
