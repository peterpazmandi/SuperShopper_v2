package com.inspirecoding.supershopper.repository.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.inspirecoding.supershopper.R
import com.inspirecoding.supershopper.data.Category
import com.inspirecoding.supershopper.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(
    entities = [Category::class], version = 1
)
abstract class ShopperDatabase : RoomDatabase() {

    abstract fun shopperDao(): ShopperDao

    class Callback @Inject constructor(
        private val database: Provider<ShopperDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().shopperDao()

            applicationScope.launch {
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_alcoholic_drinks,
                    nameStringResId = R.string.alcoholic_drinks,
                    position = 1
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_baby_products,
                    nameStringResId = R.string.baby_products,
                    position = 2
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_bakery,
                    nameStringResId = R.string.bakery,
                    position = 3
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_beverages,
                    nameStringResId = R.string.beverages,
                    position = 4
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_canned_foods,
                    nameStringResId = R.string.canned_foods,
                    position = 5
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_car_care_products,
                    nameStringResId = R.string.car_care_products,
                    position = 6
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_clothes,
                    nameStringResId = R.string.clothes,
                    position = 7
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_coffee,
                    nameStringResId = R.string.coffee_tea_and_hot_chocolate,
                    position = 8
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_condiments,
                    nameStringResId = R.string.condiments,
                    position = 9
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_cosmetics,
                    nameStringResId = R.string.cosmetics,
                    position = 10
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_dairy_product,
                    nameStringResId = R.string.dairy_products,
                    position = 11
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_diet_food,
                    nameStringResId = R.string.diet_foods,
                    position = 12
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_electrical_products,
                    nameStringResId = R.string.electronics_devices,
                    position = 13
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_seafood,
                    nameStringResId = R.string.fish_and_seafood,
                    position = 14
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_frozen_products,
                    nameStringResId = R.string.frozen,
                    position = 15
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_pasta,
                    nameStringResId = R.string.grains_and_pasta,
                    position = 16
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_kitchen,
                    nameStringResId = R.string.home_and_kitchen,
                    position = 17
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_baking,
                    nameStringResId = R.string.home_baking,
                    position = 18
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_home_cleaning,
                    nameStringResId = R.string.house_cleaning_products,
                    position = 19
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_meat,
                    nameStringResId = R.string.meat_pourly_and_sausages,
                    position = 20
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_newspaper,
                    nameStringResId = R.string.newspapers,
                    position = 21
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_office_desk,
                    nameStringResId = R.string.office_supplies,
                    position = 22
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_oils,
                    nameStringResId = R.string.oils,
                    position = 23
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_tag,
                    nameStringResId = R.string.other,
                    position = 24
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_toilette_paper,
                    nameStringResId = R.string.personal_hygiene,
                    position = 25
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_pets,
                    nameStringResId = R.string.pet_care,
                    position = 26
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_pharmacy,
                    nameStringResId = R.string.pharmacy,
                    position = 27
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_preserved_foods,
                    nameStringResId = R.string.preserves,
                    position = 28
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_ready_meal,
                    nameStringResId = R.string.ready_meals,
                    position = 29
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_snacks,
                    nameStringResId = R.string.snacks,
                    position = 30
                ))
                dao.insertCategory(Category(
                    customName = "",
                    iconDrawableResId = R.drawable.ic_spice,
                    nameStringResId = R.string.spices,
                    position = 31
                ))
            }

        }

    }


}