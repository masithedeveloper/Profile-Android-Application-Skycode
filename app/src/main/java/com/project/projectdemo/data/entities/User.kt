package com.project.projectdemo.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity
data class User(
    @PrimaryKey
    @NotNull
    var email: String,
    var name: String ?= null,
    var surname: String ?= null,
    var gender: String ?= null,
    var password: String ?= null,
    var profile_picture: String ?= null, // saved as a string/ base64
    var created_at: String ?= null,
    var updated_at: String ?= null,
)