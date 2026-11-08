package com.barakwebsoft.shopzen.domain.model

data class ShoppingItemDetail(
    val item: ShoppingItem,
    val category: Category? = null
)
