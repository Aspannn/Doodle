package kz.aspan.doodle.ui.setup.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import kz.aspan.doodle.databinding.FragmentCreateRoomBinding

class CreateRoomFragment : Fragment() {
    private var _binding: FragmentCreateRoomBinding? = null
    private val binding: FragmentCreateRoomBinding
        get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentCreateRoomBinding.bind(view)
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}