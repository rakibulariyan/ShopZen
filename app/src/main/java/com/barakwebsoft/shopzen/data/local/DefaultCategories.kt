package com.barakwebsoft.shopzen.data.local

import com.barakwebsoft.shopzen.data.local.entity.CategoryEntity

object DefaultCategories {

    val VEGETABLES = CategoryEntity(
        id = "cat_vegetables",
        name = "Vegetables",
        iconName = "eco",
        colorToken = "primary",
        isDefault = true,
        sortOrder = 0
    )

    val FRUITS = CategoryEntity(
        id = "cat_fruits",
        name = "Fruits",
        iconName = "nutrition",
        colorToken = "secondary",
        isDefault = true,
        sortOrder = 1
    )

    val DAIRY = CategoryEntity(
        id = "cat_dairy",
        name = "Dairy",
        iconName = "egg",
        colorToken = "tertiary",
        isDefault = true,
        sortOrder = 2
    )

    val BAKERY = CategoryEntity(
        id = "cat_bakery",
        name = "Bakery",
        iconName = "bakery_dining",
        colorToken = "primary",
        isDefault = true,
        sortOrder = 3
    )

    val GROCERIES = CategoryEntity(
        id = "cat_groceries",
        name = "Groceries",
        iconName = "shopping_basket",
        colorToken = "secondary",
        isDefault = true,
        sortOrder = 4
    )

    val MEAT_FISH = CategoryEntity(
        id = "cat_meat_fish",
        name = "Meat & Fish",
        iconName = "set_meal",
        colorToken = "tertiary",
        isDefault = true,
        sortOrder = 5
    )

    val CLEANING = CategoryEntity(
        id = "cat_cleaning",
        name = "Cleaning",
        iconName = "cleaning_services",
        colorToken = "primary",
        isDefault = true,
        sortOrder = 6
    )

    val PERSONAL_CARE = CategoryEntity(
        id = "cat_personal_care",
        name = "Personal Care",
        iconName = "spa",
        colorToken = "secondary",
        isDefault = true,
        sortOrder = 7
    )

    val OTHERS = CategoryEntity(
        id = "cat_others",
        name = "Others",
        iconName = "category",
        colorToken = "outline",
        isDefault = true,
        sortOrder = 8
    )

    val ALL: List<CategoryEntity> = listOf(
        VEGETABLES,
        FRUITS,
        DAIRY,
        BAKERY,
        GROCERIES,
        MEAT_FISH,
        CLEANING,
        PERSONAL_CARE,
        OTHERS
    )
}
