# Regras de ofuscação do app Burger House.
# O app não usa reflexão em classes sensíveis; mantemos apenas as regras
# recomendadas para bibliotecas que dependem de metadata (Room, Coroutines).

# Mantém anotações do Room
-keep class androidx.room.** { *; }

# Mantém classes de dados serializadas (relatórios de erro)
-keep class com.burgerhouse.app.data.local.entity.** { *; }

# Coroutines
-dontwarn kotlinx.coroutines.**
