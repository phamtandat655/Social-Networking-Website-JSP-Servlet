package org.example.j2ee.Service;

import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transaction;
import org.example.j2ee.Model.*;
import org.example.j2ee.Util.JPAUtil;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.sql.Timestamp;
import java.util.List;

public class ProfileSV {
    public User getUserById(int id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public User createUser(User user) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }
    public boolean deleteUser(int id) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            User user = em.find(User.class, id);
            if (user == null) {
                return false;
            }
            transaction.begin();
            em.remove(user);
            transaction.commit();
            return true;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public User updateUser(User user) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            user = em.merge(user);
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace(); // Log lỗi chi tiết
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }

    public List<Comment> getCommentsByPostId(int postId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            List<Comment> comments = em.createQuery("SELECT c FROM Comment c WHERE c.post.id = :postId ORDER BY c.timeline DESC")
                    .setParameter("postId", postId)
                    .getResultList();
            transaction.commit();
            return comments;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }


    public List<Reaction> getReactionsByPostId(int postId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            List<Reaction> reactions = em.createQuery("SELECT r FROM Reaction r WHERE r.post.id = :postId")
                    .setParameter("postId", postId)
                    .getResultList();
            transaction.commit();
            return reactions;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }


    public List<Post> getPostsFromUserId(int postOwnerId, int currentUserId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            List<Post> posts = em.createQuery("SELECT p FROM Post p WHERE p.user.id = :userId")
                    .setParameter("userId", postOwnerId)
                    .getResultList();

            posts.forEach(post -> {
                post.setComments(getCommentsByPostId(post.getId()));
                post.setReactions(getReactionsByPostId(post.getId()));

                // Kiểm tra nếu bài post đã được like bởi người dùng đang đăng nhập
                post.setLikedByCurrentUser(post.getReactions().stream()
                        .anyMatch(r -> r.getUser().getId() == currentUserId));
            });

            transaction.commit();
            return posts;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            em.close();
        }
    }


    public List<User> getFriendsFromUserId(int userId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String jpql = "SELECT u FROM User u " +
                    "JOIN Friend f ON (u.id = f.user1 OR u.id = f.user2) " +
                    "WHERE (f.user1 = :userId OR f.user2 = :userId) AND u.id != :userId AND f.isfriend = 1 " +
                    "GROUP BY u.id, u.name, u.email, u.avt";

            Query query = em.createQuery(jpql);
            query.setParameter("userId", userId);

            transaction.commit();
            return query.getResultList();
        }
        catch (Exception e) {
            if(transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            em.close();
        }
    }

    public long countFriends(int userId) {
        EntityManager em = JPAUtil.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        try {
            transaction.begin();
            String jpql = "SELECT COUNT(u.id) " +
                    "FROM User u " +
                    "JOIN Friend f ON (u.id = f.user1 OR u.id = f.user2) " +
                    "WHERE (f.user1 = :userId OR f.user2 = :userId) AND u.id != :userId AND f.isfriend = 1";

            Query query = em.createQuery(jpql);
            query.setParameter("userId", userId);
            transaction.commit();
            return (long) query.getSingleResult();
        }
        catch (Exception e) {
            if(transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        finally {
            em.close();
        }
    }
}
