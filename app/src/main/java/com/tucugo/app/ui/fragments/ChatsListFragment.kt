package com.tucugo.app.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.tucugo.app.R
import com.tucugo.app.data.repository.ChatRepository
import com.tucugo.app.databinding.FragmentChatsListBinding
import com.tucugo.app.ui.adapters.ChatsAdapter
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ChatsListFragment : Fragment() {

    private var _binding: FragmentChatsListBinding? = null
    private val binding get() = _binding!!

    private val chatRepository = ChatRepository()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var chatsAdapter: ChatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currentUserId = auth.currentUser?.uid ?: return

        setupRecyclerView(currentUserId)
        observeChats(currentUserId)
    }

    private fun setupRecyclerView(currentUserId: String) {
        chatsAdapter = ChatsAdapter(emptyList(), currentUserId) { chat ->
            val bundle = Bundle().apply {
                putString("relatedId", chat.id)
            }
            findNavController().navigate(R.id.action_chatsListFragment_to_chatFragment, bundle)
        }

        binding.rvChats.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = chatsAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun observeChats(userId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            chatRepository.getChats(userId).collectLatest { chats ->
                if (chats.isEmpty()) {
                    binding.tvEmptyChats.visibility = View.VISIBLE
                    binding.rvChats.visibility = View.GONE
                } else {
                    binding.tvEmptyChats.visibility = View.GONE
                    binding.rvChats.visibility = View.VISIBLE
                    chatsAdapter.updateChats(chats)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
