package com.autenticar.tuid.model;

import com.google.gson.annotations.SerializedName;

public class Answer {


  @SerializedName("id")
  public String ID;

  @SerializedName("idChoice")
  public String IDChoice;

  public Answer(String id, String idChoice) {
    this.ID = id;
    this.IDChoice = idChoice;
  }

  @Override
  public String toString() {
    return "{" +
            "id='" + ID + '\'' +
            ", idChoice='" + IDChoice + '\'' +
            '}';
  }
}

/*
"id_user": "1",
                        "answers": [
                        {"id": "0", "idChoice":"2"}
                        ]
                        }
 */

