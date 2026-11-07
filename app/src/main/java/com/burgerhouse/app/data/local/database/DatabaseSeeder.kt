package com.burgerhouse.app.data.local.database

import com.burgerhouse.app.data.local.entity.Coupon
import com.burgerhouse.app.data.local.entity.Product
import com.burgerhouse.app.domain.model.ProductCategory

/**
 * Dados iniciais do app (seed do Room).
 *
 * As imagens usam URLs públicas do Unsplash e são carregadas via Coil.
 * Todos os preços estão em Real (R$).
 */
object DatabaseSeeder {

    private const val IMAGE_BASE = "https://images.unsplash.com/photo-"

    val products: List<Product> = listOf(
        // ---------- Hambúrgueres (Clássicos) ----------
        Product(
            name = "Cheeseburger",
            shortDescription = "Clássico com cheddar derretido",
            description = "Pão brioche, blend 150g de carne bovina, queijo cheddar, alface, tomate, cebola e molho especial da casa.",
            price = 24.90,
            category = ProductCategory.BURGERS.id,
            imageUrl = "${IMAGE_BASE}1568901346375-23c9450c58cd?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Bacon Burger",
            shortDescription = "Bacon crocante e cheddar",
            description = "Blend 160g, bacon crocante, queijo cheddar duplo, cebola caramelizada, alface americana e maionese defumada.",
            price = 29.90,
            category = ProductCategory.BURGERS.id,
            imageUrl = "${IMAGE_BASE}1553979459-d22d9ba74e3b?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Duplo Cheddar",
            shortDescription = "Dois blends, cheddar duplo",
            description = "Dois blends de 120g, cheddar duplo, picles, cebola e molho burger no pão australiano.",
            price = 34.90,
            category = ProductCategory.BURGERS.id,
            imageUrl = "${IMAGE_BASE}1588364333319-8256d78e1a9a?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Smash Burger",
            shortDescription = "Smash 90g na chapa",
            description = "Dois smash burgers de 90g prensados na chapa, queijo americano, picles, cebola e molho da casa.",
            price = 27.90,
            category = ProductCategory.BURGERS.id,
            imageUrl = "${IMAGE_BASE}1528735602780-2552fd46c7af?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Veggie Burger",
            shortDescription = "Opção vegetariana",
            description = "Hambúrguer de grão-de-bico, queijo vegano, rúcula, tomate seco e maionese de castanhas.",
            price = 26.90,
            category = ProductCategory.BURGERS.id,
            imageUrl = "${IMAGE_BASE}1524745812075-063f997009d6?auto=format&fit=crop&w=600&q=80"
        ),

        // ---------- Combos ----------
        Product(
            name = "Combo Clássico",
            shortDescription = "Cheeseburger + frita + bebida",
            description = "Cheeseburger individual acompanhado de batata frita pequena e refrigerante em lata 350ml.",
            price = 39.90,
            category = ProductCategory.COMBOS.id,
            imageUrl = "${IMAGE_BASE}1615894883367-88cd2e00c25b?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Combo Duplo",
            shortDescription = "Duplo Cheddar + frita + bebida",
            description = "Duplo Cheddar acompanhado de batata frita média, anéis de cebola e refrigerante 600ml.",
            price = 49.90,
            category = ProductCategory.COMBOS.id,
            imageUrl = "${IMAGE_BASE}1607013251379-e6eecfffe0ae?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Combo Família",
            shortDescription = "Para dividir com a galera",
            description = "Dois Bacon Burgers, duas batatas grandes, onion rings e dois refrigerantes 600ml.",
            price = 89.90,
            category = ProductCategory.COMBOS.id,
            imageUrl = "${IMAGE_BASE}1594212699903-1b76adf8c1e5?auto=format&fit=crop&w=600&q=80"
        ),

        // ---------- Acompanhamentos ----------
        Product(
            name = "Batata Frita",
            shortDescription = "Crocante com sal e orégano",
            description = "Porção generosa de batata frita crocante temperada com sal e orégano.",
            price = 14.90,
            category = ProductCategory.SIDES.id,
            imageUrl = "${IMAGE_BASE}1573080407317-e69119522e52?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Batata com Cheddar e Bacon",
            shortDescription = "Coberta com cheddar e bacon",
            description = "Batata frita coberta com cheddar cremoso e bacon crocante em cubos.",
            price = 19.90,
            category = ProductCategory.SIDES.id,
            imageUrl = "${IMAGE_BASE}1532634661960-f3ef6a7381b0?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Onion Rings",
            shortDescription = "Anéis de cebola empanados",
            description = "Anéis de cebola empanados na cerveja, crocantes por fora e macios por dentro, com molho especial.",
            price = 16.90,
            category = ProductCategory.SIDES.id,
            imageUrl = "${IMAGE_BASE}1631042308844-e2e17d94527a?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Nuggets",
            shortDescription = "Frango empanado",
            description = "10 unidades de nuggets de frango empanados com molho à escolha (barbecue, mostarda e mel ou cheddar).",
            price = 15.90,
            category = ProductCategory.SIDES.id,
            imageUrl = "${IMAGE_BASE}1626082927389-6cd097cdc1ec?auto=format&fit=crop&w=600&q=80"
        ),

        // ---------- Bebidas ----------
        Product(
            name = "Coca-Cola 350ml",
            shortDescription = "Lata gelada",
            description = "Refrigerante Coca-Cola em lata de 350ml bem gelado.",
            price = 7.90,
            category = ProductCategory.DRINKS.id,
            imageUrl = "${IMAGE_BASE}1628259539347-8b5c2c41857e?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Suco Natural",
            shortDescription = "Laranja ou abacaxi",
            description = "Suco natural espremido na hora (laranja ou abacaxi), copo de 400ml.",
            price = 9.90,
            category = ProductCategory.DRINKS.id,
            imageUrl = "${IMAGE_BASE}1615381090793-b7325e86d953?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Milkshake",
            shortDescription = "Chocolate, morango ou ovomaltine",
            description = "Milkshake cremoso de 400ml nos sabores chocolate, morango ou ovomaltine.",
            price = 16.90,
            category = ProductCategory.DRINKS.id,
            imageUrl = "${IMAGE_BASE}1571270278390-65e9472f1ca2?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Água",
            shortDescription = "Mineral com ou sem gás",
            description = "Água mineral 500ml, com ou sem gás.",
            price = 4.90,
            category = ProductCategory.DRINKS.id,
            imageUrl = "${IMAGE_BASE}1601832150127-52957e97345e?auto=format&fit=crop&w=600&q=80"
        ),

        // ---------- Sobremesas ----------
        Product(
            name = "Brownie com Sorvete",
            shortDescription = "Brownie quente + sorvete",
            description = "Brownie de chocolate quente servido com bola de sorvete de creme e calda de chocolate.",
            price = 18.90,
            category = ProductCategory.DESSERTS.id,
            imageUrl = "${IMAGE_BASE}1606313564200-1e79f8916cf4?auto=format&fit=crop&w=600&q=80"
        ),
        Product(
            name = "Petit Gateau",
            shortDescription = "Bolo quente com recheio",
            description = "Bolinho de chocolate com recheio cremoso servido morno com sorvete de baunilha.",
            price = 22.90,
            category = ProductCategory.DESSERTS.id,
            imageUrl = "${IMAGE_BASE}1624355915438-12bd7bce22b8?auto=format&fit=crop&w=600&q=80"
        )
    )

    /** Cupons válidos do aplicativo. */
    val coupons: List<Coupon> = listOf(
        Coupon(
            code = "BURGER10",
            description = "10% de desconto no pedido",
            type = Coupon.TYPE_PERCENT,
            value = 10.0
        ),
        Coupon(
            code = "PRIMEIRA15",
            description = "15% de desconto na primeira compra",
            type = Coupon.TYPE_PERCENT,
            value = 15.0
        ),
        Coupon(
            code = "FRETEGRATIS",
            description = "Frete grátis",
            type = Coupon.TYPE_FREESHIP,
            value = 0.0
        )
    )
}
