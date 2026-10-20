package com.example.acadex

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.acadex.databinding.FragmentMapBinding
import com.example.acadex.databinding.ItemRoomBinding
import kotlin.math.hypot

data class CampusSpot(val name: String, val x: Float, val y: Float)

class MapFragment : Fragment() {
    private var _b: FragmentMapBinding? = null
    private val b get() = _b!!

    private val imgW = 1476f
    private val imgH = 757f

    private val spots = listOf(
        CampusSpot("Annex D", 0.268f, 0.255f),
        CampusSpot("Annex C", 0.393f, 0.255f),
        CampusSpot("CE Building", 0.698f, 0.251f),
        CampusSpot("Clinic", 0.627f, 0.288f),
        CampusSpot("Annex", 0.108f, 0.380f),
        CampusSpot("Main Building", 0.186f, 0.604f),
        CampusSpot("Guidance Office", 0.068f, 0.678f),
        CampusSpot("CIT Office", 0.051f, 0.787f),
        CampusSpot("Soccer Field", 0.446f, 0.565f),
        CampusSpot("Parking Lot", 0.437f, 0.824f),
        CampusSpot("School Canteen", 0.623f, 0.881f),
        CampusSpot("Digitel", 0.820f, 0.622f)
    )

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _b = FragmentMapBinding.inflate(i, c, false)
        return b.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        b.header.tvHeaderTitle.text = "Campus Map & Directory"
        b.header.btnBack.setOnClickListener { (requireActivity() as MainActivity).goHome() }

        // Setup PhotoView zoom and tap detection
        b.campusMap.maximumScale = 5f
        b.campusMap.setOnPhotoTapListener { _, x, y ->
            val hit = spots
                .minByOrNull { hypot((it.x - x) * imgW, (it.y - y) * imgH) }
                ?.takeIf { hypot((it.x - x) * imgW, (it.y - y) * imgH) < 90f }

            if (hit != null) {
                val matchedRoom = SampleData.rooms.find { r ->
                    r.name.contains(hit.name, ignoreCase = true) || hit.name.contains(r.name, ignoreCase = true)
                }
                if (matchedRoom != null) {
                    Dialogs.roomInfo(requireContext(), matchedRoom)
                } else {
                    Toast.makeText(requireContext(), "Selected: ${hit.name}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "No building selected", Toast.LENGTH_SHORT).show()
            }
        }

        b.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b4: Int, c: Int) { showRooms(s?.toString().orEmpty()) }
            override fun afterTextChanged(s: Editable?) {}
        })
        showRooms("")
    }

    private fun showRooms(query: String) {
        val inflater = LayoutInflater.from(requireContext())
        b.roomContainer.removeAllViews()
        val filtered = SampleData.rooms.filter {
            it.name.contains(query.trim(), ignoreCase = true) ||
                    it.number.contains(query.trim(), ignoreCase = true)
        }
        if (filtered.isEmpty()) {
            b.tvNoRooms.visibility = View.VISIBLE
            b.roomContainer.addView(b.tvNoRooms)
        } else {
            b.tvNoRooms.visibility = View.GONE
            filtered.forEach { room ->
                val row = ItemRoomBinding.inflate(inflater, b.roomContainer, false)
                row.tvName.text = room.name
                row.ivIcon.setImageResource(room.iconRes)
                row.root.setOnClickListener { Dialogs.roomInfo(requireContext(), room) }
                b.roomContainer.addView(row.root)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _b = null
    }
}
