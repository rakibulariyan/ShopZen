package com.barakwebsoft.shopzen.domain.model

data class Category(
    val id: String,
    val name: String,
    val iconName: String? = null,
    val colorToken: String? = null,
    val isDefault: Boolean = false,
    val sortOrder: Int = 0
)
