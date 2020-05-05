package com.shubham.silverscoop.adapter

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.shubham.silverscoop.R
import com.shubham.silverscoop.activity.RestaurantMenuActivity
import com.shubham.silverscoop.database.RestaurantDatabase
import com.shubham.silverscoop.database.RestaurantEntity
import com.shubham.silverscoop.models.Restaurant

import com.squareup.picasso.Picasso


class DashboardFragmentAdapter(val context: Context, var itemList: ArrayList<Restaurant>) :
    RecyclerView.Adapter<DashboardFragmentAdapter.ViewHolderDashboard>() {

    class ViewHolderDashboard(view: View) : RecyclerView.ViewHolder(view) {


        val imageViewRestaurant: ImageView = view.findViewById(R.id.imgViewRestaurant)
        val textViewRestaurantName: TextView = view.findViewById(R.id.txtViewRestaurantName)
        val textViewPricePerPerson: TextView = view.findViewById(R.id.txtViewPricePerPerson)
        val textViewRating: TextView = view.findViewById(R.id.txtViewRating)
        val llContent: LinearLayout = view.findViewById(R.id.llContent)
        val textViewfavourite: TextView = view.findViewById(R.id.txtViewfavourite)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderDashboard {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.dashboard_recycler_view_single_row, parent, false)

        return ViewHolderDashboard(view)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: ViewHolderDashboard, position: Int) {
        /*val text = arrayList.get(position)
        holder.itemView.text=text  // save the data to all the view in the dashboard recycler view single row*/

        val restaurant =
            itemList[position]//gets the item from the itemList sent in the constructor at the position
        //holder.textView.text=text//fill in the recieved data in the holder

        val restaurantEntity = RestaurantEntity(
            restaurant.restaurantId,
            restaurant.restaurantName
        )

        holder.textViewfavourite.setOnClickListener(View.OnClickListener {
            if (!DBAsyncTask(context, restaurantEntity, 1).execute().get()) {

                val result = DBAsyncTask(context, restaurantEntity, 2).execute().get()

                if (result) {

                    Toast.makeText(context, "Added to favourites", Toast.LENGTH_SHORT).show()

                    holder.textViewfavourite.setTag("liked")//new value
                    holder.textViewfavourite.background =
                        context.resources.getDrawable(R.drawable.ic_favourite)
                } else {

                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()

                }

            } else {

                val result = DBAsyncTask(context, restaurantEntity, 3).execute().get()

                if (result) {

                    Toast.makeText(context, "Removed favourites", Toast.LENGTH_SHORT).show()

                    holder.textViewfavourite.setTag("unliked")
                    holder.textViewfavourite.background =
                        context.resources.getDrawable(R.drawable.ic_favourite)
                } else {

                    Toast.makeText(context, "Some error occurred", Toast.LENGTH_SHORT).show()

                }

            }
        })


        holder.llContent.setOnClickListener(View.OnClickListener {

            println(holder.textViewRestaurantName.getTag().toString())

            val intent = Intent(context, RestaurantMenuActivity::class.java)

            intent.putExtra("restaurantId", holder.textViewRestaurantName.getTag().toString())

            intent.putExtra("restaurantName", holder.textViewRestaurantName.text.toString())


            context.startActivity(intent)


        })


        holder.textViewRestaurantName.setTag(restaurant.restaurantId + "")
        holder.textViewRestaurantName.text = restaurant.restaurantName
        holder.textViewPricePerPerson.text = restaurant.cost_for_one + "/Person "
        holder.textViewRating.text = restaurant.restaurantRating
        //holder.imageViewBook.setBackgroundResource(book.bookImage)
        Picasso.get().load(restaurant.restaurantImage).error(R.drawable.ic_restaurant_image)
            .into(holder.imageViewRestaurant);//if the image is not displayed properly we display default image in the error part


        val checkFav = DBAsyncTask(context, restaurantEntity, 1).execute()
        val isFav = checkFav.get()

        if (isFav) {
            holder.textViewfavourite.setTag("liked")
            holder.textViewfavourite.background =
                context.resources.getDrawable(R.drawable.ic_favourite)

        } else {
            holder.textViewfavourite.setTag("unliked")
            holder.textViewfavourite.background =
                context.resources.getDrawable(R.drawable.ic_unmark_favourite)
        }

    }

    fun filterList(filteredList: ArrayList<Restaurant>) {//to update the recycler view depending on the search
        itemList = filteredList
        notifyDataSetChanged()
    }


    class DBAsyncTask(val context: Context, val restaurantEntity: RestaurantEntity, val mode: Int) :
        AsyncTask<Void, Void, Boolean>() {

        val db =
            Room.databaseBuilder(context, RestaurantDatabase::class.java, "restaurant-db").build()

        override fun doInBackground(vararg p0: Void?): Boolean {

            /*
            * Mode 1->check if restaurant is in favourites
            * Mode 2->Save the restaurant into DB as favourites
            * Mode 3-> Remove the favourite restaurant*/


            when (mode) {
                1 -> {
                    val restaurant: RestaurantEntity? = db.restaurantDao()
                        .getRestaurantById(restaurantEntity.restaurantId)
                    db.close()
                    return restaurant != null
                }
                2 -> {
                    db.restaurantDao().insertRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                3 -> {
                    db.restaurantDao().deleteRestaurant(restaurantEntity)
                    db.close()
                    return true
                }
                else -> return false

            }

        }


    }
}

