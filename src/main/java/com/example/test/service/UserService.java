package com.example.test.service;

import java.util.ArrayList;
import java.util.Optional;

import com.example.test.entity.User;
import com.example.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class UserService implements UserDetailsService  {
	
	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		
		User user = userRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("등록된 사용자 없음"));
		
		return new org.springframework.security.core.userdetails.User(
				user.getUsername(),
				user.getPassword(),
				new ArrayList<>()
				);
	}

    // 아이디 중복 확인
    public boolean isUsernameDuplicated(String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        return existingUser.isPresent();
    }

    // 닉네임 중복 확인
    public boolean isNicknameDuplicated (String nickname) {
        Optional<User> existingNickname = userRepository.findByNickname(nickname);
        return existingNickname.isPresent(); // 존재하면 true (중복), 없으면 false
    }

    // 유저 찾기
    public Optional<User> getUser(String username) {
        return userRepository.findByUsername(username);
    }

    // 회원가입
    public User signup(User user) {

        return userRepository.save(user);
    }
	
}