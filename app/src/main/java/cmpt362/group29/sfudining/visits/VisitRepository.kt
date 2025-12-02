package cmpt362.group29.sfudining.visits

import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot

class VisitRepository(private val db: FirebaseFirestore) {

    private val usersCollection = db.collection("users")

    fun getVisits(userId: String, onResult: (List<Visit>) -> Unit) {
        usersCollection
            .document(userId)
            .collection("visits")
            .get()
            .addOnSuccessListener { snapshot ->
                val visits = snapshot.documents.mapNotNull { doc ->
                    val visit = doc.toObject(Visit::class.java)
                    visit?.copy(id = doc.id)
                }
                // Fetch items for each visit
                val visitsWithItems = mutableListOf<Visit>()
                val tasks = mutableListOf<Task<QuerySnapshot>>()

                visits.forEach { visit ->
                    val itemsTask = usersCollection
                        .document(userId)
                        .collection("visits")
                        .document(visit.id!!)
                        .collection("items")
                        .get()
                    tasks.add(itemsTask)
                    itemsTask.addOnSuccessListener { itemSnapshot ->
                        val items = itemSnapshot.documents.mapNotNull { itemSnapshot ->
                            val item = itemSnapshot.toObject(VisitItem::class.java)
                            item?.copy(id = itemSnapshot.id)
                        }
                        visitsWithItems.add(visit.copy(items = items))
                    }
                }

                Tasks.whenAllComplete(tasks).addOnSuccessListener {
                    onResult(visitsWithItems)
                }
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun addVisit(userId: String, visit: Visit) {
        val visitsCollection = usersCollection
            .document(userId)
            .collection("visits")

        // Generate document ID if id is null
        val docRef = if (visit.id != null) {
            visitsCollection.document(visit.id)
        } else {
            visitsCollection.document()
        }

        val visitWithId = visit.copy(id = docRef.id)

        docRef.set(visitWithId)
            .addOnSuccessListener {
                // Add items as subcollection
                val batch = db.batch()
                visitWithId.items.forEach { item ->
                    val itemRef = docRef.collection("items").document()
                    batch.set(itemRef, item)
                }
                batch.commit()
            }
    }

    fun editVisit(userId: String, visit: Visit, onResult: (Boolean) -> Unit) {
        val visitsCollection = usersCollection
            .document(userId)
            .collection("visits")

        val docRef = visitsCollection.document(visit.id!!)

        docRef.set(visit)
            .addOnSuccessListener {
                val batch = db.batch()
                // Delete existing items
                docRef.collection("items").get()
                    .addOnSuccessListener { snapshot ->
                        snapshot.documents.forEach { doc ->
                            batch.delete(doc.reference)
                        }
                        // Then add updated items
                        visit.items.forEach { item ->
                            val itemRef = docRef.collection("items").document()
                            batch.set(itemRef, item)
                        }
                        batch.commit()
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }

    fun deleteVisit(userId: String, visitId: String, onResult: (Boolean) -> Unit) {
        val visitRef = usersCollection
            .document(userId)
            .collection("visits")
            .document(visitId)

        // Delete all items in the items subcollection
        visitRef.collection("items").get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        // Delete the visit document
                        visitRef.delete()
                            .addOnSuccessListener { onResult(true) }
                            .addOnFailureListener { onResult(false) }
                    }
                    .addOnFailureListener { onResult(false) }
            }
            .addOnFailureListener { onResult(false) }
    }
}

